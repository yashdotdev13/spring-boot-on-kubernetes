# Chapter 04 - Production Readiness

In this chapter, we focus on making a Spring Boot application production-ready on Kubernetes by implementing health probes, resource management, rolling updates, rollbacks, and autoscaling.

---

## Objectives

* Deploy the application inside a dedicated namespace.
* Expose the application using a Kubernetes Service.
* Configure Startup, Liveness, and Readiness probes.
* Manage CPU and Memory using Requests and Limits.
* Perform Zero-Downtime Rolling Updates.
* Rollback to a previous version.
* Configure Horizontal Pod Autoscaler (HPA).

---

# Project Structure

```text
04-production-readiness

├── kubernetes
│   ├── namespace.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   └── hpa.yaml

├── spring-boot-app

├── diagrams

└── README.md
```

---

# Namespace

A Namespace provides logical isolation inside a Kubernetes cluster.

### Create Namespace

```bash
kubectl apply -f kubernetes/namespace.yaml
```

### Verify

```bash
kubectl get ns
```

Expected:

```text
spring-boot-prod
```

---

# Deployment

The Spring Boot application is deployed inside the `spring-boot-prod` namespace.

### Features

* 3 Replicas
* Rolling Update Strategy
* Startup Probe
* Liveness Probe
* Readiness Probe
* CPU & Memory Requests
* CPU & Memory Limits

---

# Service

Pods have dynamic IP addresses.

A Service provides:

* Stable IP
* Stable DNS
* Load Balancing across Pods

### Apply

```bash
kubectl apply -f kubernetes/service.yaml
```

### Verify

```bash
kubectl get svc -n spring-boot-prod
```

---

# Startup Probe

### Problem

Spring Boot applications may take time to start.

Without Startup Probe:

```text
Pod Starts

↓

Liveness Probe runs

↓

Application still starting

↓

Probe fails

↓

Pod restarted ❌
```

### Solution

Startup Probe delays health checks until the application has fully started.

### Endpoint

```text
/actuator/health
```

---

# Liveness Probe

Liveness Probe answers:

```text
Is the application alive?
```

If the probe fails:

```text
Kubernetes

↓

Kills Container

↓

Creates New Container
```

### Endpoint

```text
/actuator/health/liveness
```

---

# Readiness Probe

Readiness Probe answers:

```text
Can the application receive traffic?
```

If the probe fails:

```text
Application Running

↓

Removed from Service

↓

No traffic sent
```

### Endpoint

```text
/actuator/health/readiness
```

---

# Resource Requests

Requests tell Kubernetes:

```text
Reserve resources for me.
```

Example:

```yaml
requests:
  cpu: "250m"
  memory: "256Mi"
```

Meaning:

* CPU : 0.25 Core
* Memory : 256 MB

---

# Resource Limits

Limits tell Kubernetes:

```text
Do not allow me to exceed this.
```

Example:

```yaml
limits:
  cpu: "500m"
  memory: "512Mi"
```

Meaning:

* CPU : 0.5 Core
* Memory : 512 MB

---

## If Memory Limit Exceeds

```text
Application uses more than 512Mi

↓

OOMKilled

↓

Container Terminated

↓

Deployment creates new Pod
```

---

# QoS Class

Since:

```text
Requests < Limits
```

The Pod gets:

```text
QoS = Burstable
```

Kubernetes QoS Classes:

| Requests          | Limits            | QoS        |
| ----------------- | ----------------- | ---------- |
| Not Set           | Not Set           | BestEffort |
| Equal             | Equal             | Guaranteed |
| Requests < Limits | Requests < Limits | Burstable  |

---

# Rolling Updates

Rolling Updates enable Zero Downtime Deployments.

### Configuration

```yaml
strategy:

  type: RollingUpdate

  rollingUpdate:

    maxSurge: 1

    maxUnavailable: 1
```

---

## How it Works

```text
Version 1

Pod-1

Pod-2

Pod-3

↓

Deploy Version 2

↓

Create New Pod

↓

Wait for Readiness

↓

Delete Old Pod

↓

Repeat

↓

Version 2 Running
```

---

# Rollback

If a deployment fails:

```text
Version 2

↓

Bug Found

↓

Rollback

↓

Version 1 Restored
```

### Rollback Command

```bash
kubectl rollout undo deployment spring-boot-app -n spring-boot-prod
```

---

# Rollout History

View revisions:

```bash
kubectl rollout history deployment spring-boot-app -n spring-boot-prod
```

Rollback to a specific version:

```bash
kubectl rollout undo deployment spring-boot-app \
--to-revision=1 \
-n spring-boot-prod
```

---

# Horizontal Pod Autoscaler (HPA)

HPA automatically scales Pods based on CPU utilization.

### Configuration

```text
Minimum Pods : 1

Maximum Pods : 5

Target CPU : 50%
```

---

## Scaling Flow

```text
CPU = 20%

↓

1 Pod


CPU = 70%

↓

2 Pods


CPU = 90%

↓

3 Pods

↓

4 Pods

↓

5 Pods


Traffic decreases

↓

4 Pods

↓

3 Pods

↓

2 Pods

↓

1 Pod
```

---

# Verify HPA

```bash
kubectl get hpa -n spring-boot-prod
```

Example:

```text
NAME

spring-boot-app


TARGETS

2%/50%


MINPODS

1


MAXPODS

5


REPLICAS

3
```

---

# Important Commands

### Get Resources

```bash
kubectl get all -n spring-boot-prod
```

### Watch Pods

```bash
kubectl get pods -n spring-boot-prod -w
```

### Describe Pod

```bash
kubectl describe pod <pod-name> -n spring-boot-prod
```

### View Logs

```bash
kubectl logs <pod-name> -n spring-boot-prod
```

### Restart Deployment

```bash
kubectl rollout restart deployment spring-boot-app -n spring-boot-prod
```

### Rollback Deployment

```bash
kubectl rollout undo deployment spring-boot-app -n spring-boot-prod
```

---

# What We Learned

* Namespace Isolation
* Kubernetes Service
* Startup Probe
* Liveness Probe
* Readiness Probe
* CPU Requests
* Memory Requests
* CPU Limits
* Memory Limits
* QoS Classes
* Rolling Updates
* Rollbacks
* Horizontal Pod Autoscaler

---

# Next Chapter

➡️ **Chapter 05 - Stateful Applications**

Topics:

* Persistent Volumes (PV)
* Persistent Volume Claims (PVC)
* Storage Classes
* StatefulSets
* MySQL StatefulSet
* Persistent Storage on Kubernetes
