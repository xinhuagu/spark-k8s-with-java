# Spark Operator REST API

A Spring Boot REST API for managing Spark applications using the Spark Kubernetes Operator.

## Prerequisites

- Java 17+
- Maven 3.6+
- Kubernetes cluster with Spark Kubernetes Operator installed
- kubectl configured to access your cluster

## Build and Run

```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/spark-operator-api-1.0.0.jar
```

The application will start on port 8080.

## API Endpoints


## Configuration

The application uses the default Kubernetes configuration from your `~/.kube/config` file. Make sure your kubectl is properly configured to access the cluster where the Spark Operator is installed.

## Docker Image

The default Docker image used is `xinhua/spark-app:v4` as specified in the requirements. You can override this in the request payload.

