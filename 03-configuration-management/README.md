# Chapter 03 - Configuration Management

## Overview

In Chapter 02, we learned how to deploy and expose a Spring Boot application on Kubernetes using Pods, Deployments, and Services.

In this chapter, we focus on one of the most important aspects of cloud-native applications:

**Externalizing application configuration.**

Instead of hardcoding application settings inside the source code or Docker image, Kubernetes allows applications to receive configuration dynamically at runtime using:

* ConfigMaps
* Secrets
* Environment Variables
* Spring Profiles
* Volume Mounts

The objective of this chapter is to understand how Spring Boot applications can be configured without rebuilding container images and how the same Docker image can be deployed to multiple environments with different configurations.

---

# Learning Objectives

After completing this chapter, you should be able to:

* Understand the need for externalized configuration.
* Create and use ConfigMaps.
* Create and use Secrets.
* Inject ConfigMaps as Environment Variables.
* Inject Secrets as Environment Variables.
* Use `envFrom`, `configMapRef`, and `secretRef`.
* Understand Spring Profiles.
* Use `application.properties` and `application-dev.properties`.
* Activate profiles using Kubernetes ConfigMaps.
* Mount ConfigMaps as files.
* Understand runtime configuration.
* Follow the Build Once Deploy Anywhere principle.

---

# Why Configuration Management?

Suppose a Spring Boot application contains:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb

spring.datasource.username=postgres

spring.datasource.password=password
```

This works locally.

But what happens when:

### Development

```text
Database = postgres-dev
```

### Staging

```text
Database = postgres-staging
```

### Production

```text
Database = postgres-prod
```

Should we create:

```text
spring-boot-app-dev

spring-boot-app-staging

spring-boot-app-prod
```

No.

This creates:

* Multiple Docker images
* Multiple builds
* Difficult deployments
* Inconsistent environments

---

# Kubernetes Philosophy

Kubernetes follows:

```text
Build Once

Deploy Anywhere
```

Meaning:

```text
One Docker Image

↓

Many Configurations

↓

Many Environments
```

The application code remains the same.

Only configuration changes.

---

# Configuration Options in Kubernetes

Kubernetes provides two important resources:

---

## ConfigMap

ConfigMaps store:

* Application Names
* URLs
* Feature Flags
* Environment Names
* Ports
* Profiles

ConfigMaps are used for:

```text
Non-Sensitive Data
```

---

## Secret

Secrets store:

* Database Passwords
* JWT Secrets
* API Keys
* OAuth Credentials
* Certificates

Secrets are used for:

```text
Sensitive Data
```

---

# Project Structure

```text
03-configuration-management

├── kubernetes

│   ├── configmap.yaml

│   ├── secret.yaml

│   ├── profile-configmap.yaml

│   ├── file-configmap.yaml

│   ├── deployment.yaml

│   └── service.yaml


├── spring-boot-app

│   ├── Dockerfile

│   ├── pom.xml

│   └── src

│       └── main

│           └── resources

│               ├── application.properties

│               └── application-dev.properties


└── README.md
```

---

# ConfigMap

ConfigMaps store non-sensitive configuration.

Example:

```yaml
apiVersion: v1

kind: ConfigMap

metadata:

  name: spring-boot-config


data:

  APP_NAME: Spring-Boot-on-Kubernetes

  ENVIRONMENT: development
```

---

## Applying ConfigMap

```bash
kubectl apply -f configmap.yaml
```

---

## Verifying ConfigMap

```bash
kubectl get configmaps

kubectl describe configmap spring-boot-config
```

---

# Secret

Secrets store sensitive information.

Example:

```yaml
apiVersion: v1

kind: Secret

metadata:

  name: spring-boot-secret


type: Opaque


stringData:

  DB_USERNAME: postgres

  DB_PASSWORD: change-me
```

---

## Applying Secret

```bash
kubectl apply -f secret.yaml
```

---

## Verifying Secret

```bash
kubectl get secrets

kubectl describe secret spring-boot-secret
```

Output:

```text
DB_USERNAME : 8 bytes

DB_PASSWORD : 9 bytes
```

Kubernetes hides Secret values.

---

# Injecting Environment Variables

Environment variables are injected using:

```yaml
envFrom
```

---

Example:

```yaml
envFrom:

  - configMapRef:

      name: spring-boot-config


  - secretRef:

      name: spring-boot-secret
```

This injects:

```text
APP_NAME

ENVIRONMENT

DB_USERNAME

DB_PASSWORD
```

inside the container.

---

# Spring Boot Integration

Spring Boot reads Environment Variables using:

```java
@Value("${APP_NAME:Default App}")

private String appName;
```

---

Example:

```java
@Value("${ENVIRONMENT:local}")

private String environment;
```

---

Example:

```java
@Value("${DB_USERNAME:unknown}")

private String dbUsername;
```

---

# Runtime Configuration Flow

```text
ConfigMap

APP_NAME

ENVIRONMENT

       │

       ▼


Secret

DB_USERNAME

DB_PASSWORD

       │

       ▼


Deployment

       │

       ▼


Pod Environment Variables

       │

       ▼


Spring Boot

@Value

       │

       ▼


REST API Response
```

---

# Spring Profiles

Spring Profiles allow different configurations for different environments.

Example:

```text
application.properties

application-dev.properties

application-prod.properties

application-staging.properties
```

---

## application.properties

Default configuration:

```properties
spring.application.name=spring-boot-app

server.port=8081
```

---

## application-dev.properties

Development configuration:

```properties
app.message=Running from DEV Profile

app.owner=Yash

app.version=1.0
```

---

# Activating Profiles

Profiles are activated using:

```text
SPRING_PROFILES_ACTIVE
```

Example:

```yaml
apiVersion: v1

kind: ConfigMap

metadata:

  name: profile-config


data:

  SPRING_PROFILES_ACTIVE: dev
```

---

## Applying Profile ConfigMap

```bash
kubectl apply -f profile-configmap.yaml
```

---

## Verify

```bash
kubectl describe configmap profile-config
```

Output:

```text
SPRING_PROFILES_ACTIVE

dev
```

---

# Spring Profile Flow

```text
Kubernetes ConfigMap

SPRING_PROFILES_ACTIVE=dev

        │

        ▼


Pod Environment Variables

        │

        ▼


Spring Boot

        │

        ▼


application-dev.properties

        │

        ▼


DEV Configuration Loaded
```

---

# ConfigMap As File

Large configuration files are difficult to maintain using Environment Variables.

Instead:

```text
ConfigMap

↓

Volume

↓

File

↓

Spring Boot
```

---

## file-configmap.yaml

```yaml
apiVersion: v1

kind: ConfigMap

metadata:

  name: file-config


data:

  app-config.properties: |

    custom.message=ConfigMap Mounted As File

    custom.owner=Yash

    custom.version=1.0
```

---

# Volume Mount

Inside Deployment:

```yaml
volumeMounts:

  - name: config-volume

    mountPath: /config

    readOnly: true
```

---

And:

```yaml
volumes:

  - name: config-volume

    configMap:

      name: file-config
```

---

# Mounted File

Inside container:

```text
/config

└── app-config.properties
```

Content:

```properties
custom.message=ConfigMap Mounted As File

custom.owner=Yash

custom.version=1.0
```

---

# Verifying Volume Mount

Enter container:

```bash
kubectl exec -it <pod-name> -- sh
```

Navigate:

```bash
cd /config

ls
```

Output:

```text
app-config.properties
```

Read file:

```bash
cat app-config.properties
```

---

# Build Once Deploy Anywhere

This is the most important concept of this chapter.

The same Docker image:

```text
spring-boot-config-app:1.0
```

can run in:

### Development

```text
SPRING_PROFILES_ACTIVE=dev
```

---

### Staging

```text
SPRING_PROFILES_ACTIVE=staging
```

---

### Production

```text
SPRING_PROFILES_ACTIVE=prod
```

without rebuilding:

```text
spring-boot-config-app:1.0
```

Only the configuration changes.

---

# Hands-On Exercises Completed

During this chapter we:

✅ Created ConfigMap

✅ Created Secret

✅ Injected ConfigMap as Environment Variables

✅ Injected Secret as Environment Variables

✅ Used `envFrom`

✅ Used `configMapRef`

✅ Used `secretRef`

✅ Created Spring Profiles

✅ Activated Profiles using ConfigMap

✅ Created `application-dev.properties`

✅ Mounted ConfigMap as File

✅ Created Volume Mount

✅ Verified mounted files using `kubectl exec`

✅ Demonstrated runtime configuration

✅ Followed Build Once Deploy Anywhere principle

---

# Key Takeaways

## ConfigMap

Used for:

```text
Non-Sensitive Data
```

---

## Secret

Used for:

```text
Sensitive Data
```

---

## Spring Profiles

Allow environment-specific configuration.

---

## Environment Variables

Provide runtime configuration.

---

## Volume Mounts

Expose ConfigMaps as files inside containers.

---

## Build Once Deploy Anywhere

Build Docker image once.

Deploy everywhere with different configurations.

---

# Chapter Summary

In this chapter, we explored how Spring Boot applications receive configuration dynamically in Kubernetes.

We learned how to:

* Create ConfigMaps
* Create Secrets
* Inject Environment Variables
* Use Spring Profiles
* Mount ConfigMaps as files
* Externalize configuration completely

These concepts are fundamental for building production-grade Spring Boot microservices on Kubernetes and are used extensively in real-world cloud-native applications.

In the next chapter, we will focus on:

* Liveness Probes
* Readiness Probes
* Startup Probes
* Resource Requests
* Resource Limits
* Rolling Updates
* Rollbacks
* Horizontal Pod Autoscaler

to make Spring Boot applications production-ready on Kubernetes.
