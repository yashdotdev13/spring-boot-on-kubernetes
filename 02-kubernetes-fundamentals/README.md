# Chapter 02 - Kubernetes Fundamentals

## Overview

In Chapter 01, we learned how to package a Spring Boot application and run it inside Docker containers.

In this chapter, we take the next step and learn how Kubernetes manages, scales, heals, and exposes containerized Spring Boot applications.

The objective is not only to learn Kubernetes YAML syntax but to understand the core concepts that power modern cloud-native applications.

By the end of this chapter, we will understand how Kubernetes manages application lifecycle, networking, scaling, and recovery using Pods, Deployments, ReplicaSets, and Services.

---

# Learning Objectives

After completing this chapter, you should be able to:

* Understand Kubernetes architecture.
* Understand Nodes and Clusters.
* Understand Pods and why they exist.
* Understand Deployments and desired state management.
* Understand ReplicaSets.
* Understand Kubernetes Services.
* Understand ClusterIP and NodePort Services.
* Understand Labels and Selectors.
* Understand Self-Healing.
* Understand Scaling.
* Understand Namespaces.
* Use essential kubectl debugging commands.
* Deploy a Spring Boot application to Kubernetes.

---

# Kubernetes Architecture

Kubernetes is a container orchestration platform.

It automates:

* Deployment
* Scaling
* Networking
* Recovery
* Scheduling

Instead of manually managing containers, Kubernetes continuously ensures that the desired state of the application matches the actual state.

---

## High-Level Architecture

```text
Kubernetes Cluster
│
├── Control Plane
│
└── Worker Nodes
```

---

## Control Plane

The control plane is the brain of Kubernetes.

Responsibilities:

* Scheduling Pods
* Managing cluster state
* Monitoring resources
* Handling scaling operations
* Maintaining desired state

Main Components:

```text
kube-apiserver
etcd
kube-scheduler
controller-manager
```

---

## Worker Node

Worker nodes run application workloads.

A node contains:

```text
Container Runtime
Kubelet
Kube Proxy
Pods
```

In this chapter we used:

```text
Minikube
```

which provides a single-node Kubernetes cluster suitable for local development.

---

# Kubernetes Mental Model

Docker focuses on containers:

```text
Container
```

Kubernetes focuses on:

```text
Pod
```

and higher-level abstractions.

Kubernetes is a declarative system.

Instead of saying:

```text
Run this container.
```

we declare:

```text
I want three replicas running.
```

Kubernetes continuously works to achieve that desired state.

---

# Pod

## What Is A Pod?

A Pod is the smallest deployable unit in Kubernetes.

Pods wrap one or more containers.

```text
Pod
│
└── Container
```

Most Spring Boot applications follow:

```text
1 Pod
=
1 Container
```

---

## Why Pods Exist

Pods allow multiple tightly coupled containers to:

* Share networking
* Share storage
* Share lifecycle

Example:

```text
Pod
│
├── Spring Boot Application
│
└── Log Collector
```

---

## Pod YAML

```yaml
apiVersion: v1
kind: Pod

metadata:
  name: spring-boot-app

spec:
  containers:
    - name: spring-boot-app
      image: spring-boot-app:1.0
```

---

## Pod Lifecycle

When a Pod is created:

```text
Scheduled
    ↓
Container Created
    ↓
Container Started
    ↓
Running
```

Pods are temporary resources.

If a Pod is manually deleted:

```bash
kubectl delete pod spring-boot-app
```

the Pod disappears permanently.

---

# Deployment

## Problem With Pods

Pods alone are not suitable for production.

If a Pod crashes:

```text
Application Down
```

There is no mechanism to recreate it.

---

## Solution: Deployment

A Deployment manages Pods and ensures the desired number of replicas remain running.

Example:

```yaml
spec:
  replicas: 3
```

This means:

```text
Always keep 3 Pods running.
```

---

## Deployment Architecture

```text
Deployment
     │
     ▼
ReplicaSet
     │
     ▼
Pods
```

---

## Deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment

metadata:
  name: spring-boot-app

spec:
  replicas: 3

  selector:
    matchLabels:
      app: spring-boot-app

  template:
    metadata:
      labels:
        app: spring-boot-app

    spec:
      containers:
        - name: spring-boot-app
          image: spring-boot-app:1.0
```

---

# ReplicaSet

ReplicaSets are responsible for maintaining the desired number of Pods.

Example:

```text
Desired Pods = 3
Current Pods = 2
```

ReplicaSet automatically creates another Pod.

---

## Self-Healing

We demonstrated self-healing by deleting a Pod.

```bash
kubectl delete pod spring-boot-app-xxxx
```

Kubernetes automatically created:

```text
spring-boot-app-yyyy
```

because the Deployment required:

```text
replicas = 3
```

This feature is known as:

```text
Self-Healing
```

---

# Scaling

Scaling changes the number of application replicas.

---

## Declarative Scaling

Update:

```yaml
replicas: 3
```

Apply:

```bash
kubectl apply -f deployment.yaml
```

---

## Imperative Scaling

```bash
kubectl scale deployment spring-boot-app --replicas=5
```

Result:

```text
Pod 1
Pod 2
Pod 3
Pod 4
Pod 5
```

---

# Services

## Why Services Exist

Pods receive dynamic IP addresses.

Example:

```text
10.244.0.77
```

After recreation:

```text
10.244.0.95
```

The IP changes.

Applications cannot depend on Pod IPs.

---

## Service Solution

Services provide:

* Stable networking
* Stable DNS
* Load balancing
* Service discovery

---

## Service Architecture

```text
Service
    │
    ├── Pod 1
    ├── Pod 2
    └── Pod 3
```

Applications communicate through Services rather than Pods.

---

# Labels And Selectors

Labels are metadata attached to resources.

Example:

```yaml
labels:
  app: spring-boot-app
```

---

Selectors use labels to locate resources.

Example:

```yaml
selector:
  app: spring-boot-app
```

The Service discovers Pods through labels.

---

## Example

Pod:

```yaml
labels:
  app: spring-boot-app
```

Service:

```yaml
selector:
  app: spring-boot-app
```

Result:

```text
Service
    ↓
Find Pods
    ↓
app=spring-boot-app
```

---

# ClusterIP Service

ClusterIP is the default Service type.

Example:

```yaml
type: ClusterIP
```

Characteristics:

* Internal only
* Accessible inside the cluster
* Stable Cluster IP

Example:

```text
10.111.101.73
```

---

## Service Discovery

We observed:

```text
Endpoints:
10.244.0.77:8081
10.244.0.78:8081
10.244.0.79:8081
```

Kubernetes automatically discovered all Pods matching the Service selector.

---

# NodePort Service

NodePort exposes applications outside the cluster.

Example:

```yaml
type: NodePort
```

```yaml
nodePort: 30081
```

---

## Request Flow

```text
Browser
    │
    ▼
NodePort Service
    │
    ▼
ClusterIP Service
    │
    ▼
Pods
```

---

## NodePort Example

```yaml
apiVersion: v1
kind: Service

metadata:
  name: spring-boot-app-nodeport

spec:
  selector:
    app: spring-boot-app

  ports:
    - port: 8081
      targetPort: 8081
      nodePort: 30081

  type: NodePort
```

---

# Load Balancing

The Service automatically distributes traffic among Pods.

Example:

```text
Service
     │
     ├── Pod 1
     ├── Pod 2
     └── Pod 3
```

Requests are balanced across available replicas.

No additional configuration is required.

---

# Namespaces

Namespaces provide logical isolation within a cluster.

Without namespaces:

```text
All applications mixed together
```

With namespaces:

```text
Cluster
│
├── spring-boot-demo
│
├── tradeflux
│
└── kube-system
```

---

## Creating A Namespace

```bash
kubectl create namespace spring-boot-demo
```

---

## Viewing Namespaces

```bash
kubectl get ns
```

---

## Benefits

* Resource isolation
* Team separation
* Environment separation
* Better organization

---

# Essential kubectl Commands

## Get Resources

```bash
kubectl get pods

kubectl get deployments

kubectl get services

kubectl get namespaces
```

---

## Detailed Information

```bash
kubectl describe pod <pod-name>

kubectl describe deployment <deployment-name>

kubectl describe service <service-name>
```

---

## Logs

```bash
kubectl logs <pod-name>
```

Equivalent to:

```bash
docker logs
```

---

## Execute Inside Container

```bash
kubectl exec -it <pod-name> -- sh
```

Useful for:

* Checking files
* Verifying environment variables
* Debugging applications

---

## Delete Resources

```bash
kubectl delete pod <pod-name>

kubectl delete deployment <deployment-name>

kubectl delete service <service-name>
```

---

## Scaling

```bash
kubectl scale deployment spring-boot-app --replicas=5
```

---

## Wide Output

```bash
kubectl get pods -o wide
```

Displays:

* Pod IP
* Node
* Additional networking information

---

# Hands-On Exercises Completed

During this chapter we:

✅ Started Minikube

✅ Connected to Kubernetes cluster

✅ Created a Pod

✅ Inspected Pod details

✅ Viewed Pod logs

✅ Used port-forwarding

✅ Created a Deployment

✅ Created a ReplicaSet

✅ Demonstrated self-healing

✅ Scaled from 1 Pod to 3 Pods

✅ Created a ClusterIP Service

✅ Created a NodePort Service

✅ Accessed application externally

✅ Created a Namespace

✅ Executed commands inside a Pod

✅ Inspected Kubernetes networking

---

# Key Takeaways

## Pods

Pods are the smallest deployable unit in Kubernetes.

---

## Deployments

Deployments manage application lifecycle and desired state.

---

## ReplicaSets

ReplicaSets maintain the required number of Pods.

---

## Services

Services provide stable networking and service discovery.

---

## Labels & Selectors

Labels connect Kubernetes resources together.

---

## Self-Healing

Kubernetes automatically replaces failed Pods.

---

## Scaling

Applications can be scaled declaratively or imperatively.

---

## Namespaces

Namespaces provide logical isolation and organization.

---

# Chapter Summary

In this chapter, we deployed a Spring Boot application to Kubernetes and explored the fundamental building blocks of the platform.

We learned how Pods run containers, how Deployments maintain desired state, how ReplicaSets ensure availability, and how Services provide networking and load balancing.

These concepts form the foundation of every Kubernetes application and will be used extensively throughout the remaining chapters.

In the next chapter, we will explore Configuration Management using:

* ConfigMaps
* Secrets
* Environment Variables
* Spring Boot Configuration Integration

which allows applications to be configured dynamically without rebuilding container images.
