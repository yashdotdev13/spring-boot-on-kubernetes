# Chapter 01 - Containerization

## Overview

In this chapter, we explore how a Spring Boot application is packaged, containerized, and prepared for deployment in cloud-native environments.

The goal is not just to learn Docker commands, but to understand the complete journey of a Spring Boot application:

```text
Source Code
    ↓
Compilation
    ↓
Packaging (Fat JAR)
    ↓
Docker Image
    ↓
Docker Container
    ↓
Kubernetes (Upcoming Chapters)
```

By the end of this chapter, we understand how Spring Boot applications are transformed into portable, reproducible artifacts that can run consistently across different environments.

---

# Learning Objectives

After completing this chapter, you should be able to:

* Understand how Java source code is compiled.
* Explain the purpose of packaging.
* Understand what a JAR file is.
* Differentiate between a standard JAR and a Spring Boot Fat JAR.
* Build and run a Spring Boot application using Maven.
* Create Docker images for Spring Boot applications.
* Understand Docker images and containers.
* Work with environment variables.
* Understand Docker layers and caching.
* Create multi-stage Docker builds.
* Understand Spring Boot layered JARs.
* Understand the role of Buildpacks in modern cloud-native development.

---

# Project Structure

```text
01-containerization
│
├── spring-boot-app
│   ├── src
│   ├── target
│   ├── pom.xml
│   ├── Dockerfile
│   ├── Dockerfile.multistage
│   └── Dockerfile.layered
│
├── diagrams
│
└── README.md
```

---

# Spring Boot Application

A simple REST API is used throughout this chapter.

Endpoint:

```http
GET /hello
```

Response:

```json
{
  "message": "Hello from Spring Boot"
}
```

The application runs on:

```text
http://localhost:8081
```

---

# Understanding Packaging

## Why Packaging Exists

The JVM cannot execute Java source files directly.

For example:

```java
public class HelloController {
}
```

must first be compiled into bytecode.

Compilation converts:

```text
HelloController.java
```

into:

```text
HelloController.class
```

using the Java compiler.

---

## Maven Package Lifecycle

Build the application:

```bash
mvn clean package
```

Generated artifact:

```text
target/
└── spring-boot-app-0.0.1-SNAPSHOT.jar
```

Packaging bundles:

* Application classes
* Resources
* Dependencies
* Metadata

into a single distributable artifact.

---

# What Is a JAR?

JAR stands for:

```text
Java Archive
```

A JAR is essentially a ZIP file with additional metadata.

It allows Java applications to be distributed as a single artifact.

Traditional Java applications often required:

```text
my-app.jar
spring-web.jar
jackson.jar
mysql.jar
...
```

which made deployment difficult.

---

# Spring Boot Fat JAR

Spring Boot solves this problem using a Fat JAR.

A Fat JAR contains:

```text
Application Code
+
Spring Framework
+
Embedded Tomcat
+
Dependencies
+
Boot Loader
```

Everything required to run the application is packaged together.

Inspecting the JAR:

```bash
jar tf target/*.jar
```

reveals:

```text
BOOT-INF/
META-INF/
org/springframework/boot/loader/
```

---

# Running the Application

Using Maven:

```bash
mvn spring-boot:run
```

Using the packaged artifact:

```bash
java -jar target/spring-boot-app-0.0.1-SNAPSHOT.jar
```

This demonstrates that the JAR itself is executable.

---

# Docker Fundamentals

## What Is a Docker Image?

A Docker image is a read-only template used to create containers.

Think of it as:

```text
Blueprint
Template
Recipe
```

Images are immutable.

---

## What Is a Docker Container?

A container is a running instance of an image.

Think of it as:

```text
Virtual Process
```

created from a Docker image.

---

# Building the First Docker Image

Dockerfile:

```dockerfile
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
```

Build:

```bash
docker build -t spring-boot-app:1.0 .
```

Run:

```bash
docker run -p 8081:8081 spring-boot-app:1.0
```

---

# Port Mapping

Container ports are isolated.

To expose the application:

```bash
docker run -p 8081:8081 spring-boot-app:1.0
```

Mapping:

```text
Host Port       Container Port
--------------------------------
8081     --->      8081
```

Without port mapping, the application cannot be accessed from the host machine.

---

# Environment Variables

Application property:

```properties
app.message=Hello from Spring Boot
```

Injected into the application:

```java
@Value("${app.message}")
private String message;
```

Override using Docker:

```bash
docker run \
-e APP_MESSAGE="Hello from Docker" \
-p 8081:8081 \
spring-boot-app:1.0
```

Spring Boot automatically maps:

```text
app.message
```

to:

```text
APP_MESSAGE
```

This becomes extremely important when using:

* ConfigMaps
* Secrets
* Kubernetes Deployments

in later chapters.

---

# Docker Layers

Every Docker instruction creates a layer.

Example:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

Creates:

```text
Layer 1 -> Base Image
Layer 2 -> Working Directory
Layer 3 -> Application JAR
Layer 4 -> Entry Point
```

---

## Why Layers Matter

Docker caches layers.

If only application code changes:

```text
Layer 1 reused
Layer 2 reused
Layer 3 rebuilt
Layer 4 reused
```

This dramatically improves build performance.

---

# Multi-Stage Builds

## Problem

Traditional approach:

```text
Local Machine
    ↓
Maven Build
    ↓
JAR
    ↓
Docker Image
```

Docker depends on a pre-built artifact.

---

## Solution

Multi-stage builds move compilation inside Docker.

Dockerfile:

```dockerfile
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
```

---

## Benefits

* Reproducible builds
* CI/CD friendly
* Cleaner images
* Build tools excluded from final image

---

# Spring Boot Layered JARs

Spring Boot logically separates the application into layers.

Available layers:

```text
dependencies
spring-boot-loader
snapshot-dependencies
application
```

Purpose:

```text
Stable Dependencies
        ↓
Rarely Change

Application Code
        ↓
Changes Frequently
```

This improves Docker layer caching.

---

## Why Layered JARs Matter

Without layered JARs:

```text
Application Change
      ↓
Entire JAR Changes
      ↓
Docker Rebuilds Layer
```

With layered JARs:

```text
Application Change
      ↓
Only Application Layer Changes
      ↓
Dependencies Reused
```

This results in:

* Faster builds
* Faster pushes
* Faster pulls
* Faster Kubernetes deployments

---

# Buildpacks

Modern Spring Boot applications often use Buildpacks.

Instead of maintaining a Dockerfile:

```bash
mvn spring-boot:build-image
```

Buildpacks automatically:

```text
Compile Application
        ↓
Analyze Layers
        ↓
Create OCI Image
        ↓
Store in Docker
```

Benefits:

* No Dockerfile required
* Production-ready images
* Optimized layering
* Better developer experience

This approach is widely used in cloud-native environments.

---

# Key Takeaways

## Packaging

* Java source code must be compiled.
* Packaging creates a distributable artifact.
* Spring Boot produces executable Fat JARs.

## Docker

* Images are templates.
* Containers are running instances.
* Docker provides portability and consistency.

## Configuration

* Environment variables override application properties.
* Configuration should be externalized.

## Multi-Stage Builds

* Build inside Docker.
* Produce cleaner runtime images.

## Layering

* Docker layers improve caching.
* Spring Boot layered JARs optimize rebuild performance.

## Buildpacks

* Modern alternative to Dockerfiles.
* Produce optimized OCI images automatically.

---

# Chapter Summary

This chapter established the foundation for running Spring Boot applications in containerized environments.

We learned how applications move from source code to executable artifacts, how Docker packages and runs those artifacts, and how modern techniques such as multi-stage builds, layered JARs, and Buildpacks improve the developer experience.

In the next chapter, we will deploy this same Spring Boot application into Kubernetes and explore the fundamental Kubernetes resources required to run containerized applications in a cluster.
