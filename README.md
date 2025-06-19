# Spark on Kubernetes Demo Project

This project demonstrates multiple approaches to running Apache Spark applications on Kubernetes, showcasing different submission methods with java microservice frameworks.

## 📁 Project Structure

```
spark-demo/
├── infra/          # K8s infrastructure components
│   ├── k8s-db/             # PostgreSQL database setup
│   ├── k8s-history-server/ # Spark History Server
│   └── k8s-permission/     # RBAC permissions for Spark
├── spark-application/       # Spring Boot Spark application
├── spark-operator/         # Spring Boot REST API (Apache Spark Operator)
├── spark-orchestrator/     # Quarkus REST API (Direct K8s submission)
└── README.md               # This file
```

## 🚀 Quick Start

### Prerequisites

1. **Local Kubernetes cluster** (one of the following):
   - Docker Desktop with Kubernetes enabled
   - Minikube
   - Kind cluster

2. **Tools required**:
   - `kubectl` configured to access your cluster
   - `helm` (for Apache Spark Operator installation)
   - Java 17+
   - Maven 3.6+

### 🔧 Setup Instructions

#### 1. Apply Infrastructure

```bash
# Apply all Kubernetes manifests for infrastructure
kubectl apply -f infrastructure/k8s-db/
kubectl apply -f infrastructure/k8s-history-server/
kubectl apply -f infrastructure/k8s-permission/

# Verify infrastructure is running
kubectl get pods
kubectl get services
```

#### 2. Install Apache Spark Kubernetes Operator

```bash
helm repo add spark-kubernetes-operator https://apache.github.io/spark-kubernetes-operator
helm repo update
helm install spark-kubernetes-operator spark-kubernetes-operator/spark-kubernetes-operator
```



#### 3. Build Spark Application Docker Image

```bash
cd spark-application
mvn clean package
docker build -t spark-app:latest .

# If using minikube, load image into minikube
# minikube image load spark-app:latest
# for kind cluster, use: kind load docker-image spark-app:latest --name <cluster-name>
```

## 📦 Components Overview

### 🗄️ Infrastructure

**Location**: `infrastructure/`

Contains Kubernetes manifests for supporting services:

- **PostgreSQL Database** (`k8s-db/`): Database with sample user table for Spark queries
- **Spark History Server** (`k8s-history-server/`): Web UI for monitoring Spark applications
- **RBAC Permissions** (`k8s-permission/`): Service accounts and roles for Spark workloads

**Services exposed**:
- PostgreSQL: `postgres-service:5432`
- Spark History Server: `spark-history-server-service:18080`
- Spark History Server local URL: http://localhost:31000/

### 🎯 Spark Application

**Location**: `spark-application/`  
**Type**: Spring Boot + Apache Spark

A simple Spark application that:
- Connects to PostgreSQL database
- Queries the `users` table
- Processes data and writes results
- Demonstrates Spark-PostgreSQL integration

**Key files**:
- `SparkApplication.java`: Main Spark job logic
- `SparkService.java`: Business logic for data processing
- `ConfigProperties.java`: Configuration management
- `Dockerfile`: Container image definition

**Usage**:
```bash
cd spark-application
mvn clean package
java -jar target/spark-application-*.jar
```

### 🎛️ Submit Spark App with operator (Spring Boot)

**Location**: `spark-operator/`  
**Type**: Spring Boot REST API  
**Port**: 8080

REST API server that submits Spark applications using the **Apache Spark Kubernetes Operator**.

**Features**:
- Submit Spark applications via REST API
- Monitor application status
- List running applications
- Delete applications
- Uses `SparkApplication` CRD

**API Endpoints**:
```bash
# Submit Spark application
GET /spark/submit
( return a Job ID )

# Check application status
GET /spark/status/{jobId}
( return Job status)
```

**Usage**:
```bash
cd spark-operator
mvn clean package
mvn spring-boot:run

# Test API
curl -X GET http://localhost:8080/spark/submit
```

### ⚡ Submit Spark App without Operator (Quarkus)

**Location**: `spark-orchestrator/`  
**Type**: Quarkus REST API  
**Port**: 8080

Lightweight REST API that submits Spark applications **directly to Kubernetes** without using the Spark Operator.

**Features**:
- Direct Kubernetes API integration
- Uses `spark-submit` with Kubernetes master
- Faster submission (no operator dependency)
- Native compilation support with GraalVM

**API Endpoints**:
```bash
# Submit Spark application
GET /api/spark/submit

```

**Usage**:
```bash
cd spark-orchestrator
mvn clean package
mvn quarkus:dev

# Test API
curl -X GET http://localhost:8081/spark/submit
```


## 🎯 Key Differences

| Feature | Spark Operator | Spark Orchestrator |
|---------|----------------|-------------------|
| **Dependency** | Apache Spark K8s Operator | Direct Kubernetes API |
| **Submission** | SparkApplication CRD | Native spark-submit |
| **Monitoring** | Operator status + History Server | Kubernetes pods + History Server |
| **Performance** | Operator overhead | Direct submission |
| **Features** | Rich CRD features | Basic submission |
| **Complexity** | Higher (operator required) | Lower (direct K8s) |

## 🛠️ Development

### Building All Components

```bash
# Build Spark application
cd spark-application && mvn clean package

# Build Spark Operator API
cd spark-operator && mvn clean package

# Build Spark Orchestrator
cd spark-orchestrator && mvn clean package
```

## 📚 References

- [Apache Spark on Kubernetes](https://spark.apache.org/docs/latest/running-on-kubernetes.html)
- [Spark Kubernetes Operator](https://github.com/apache/spark-kubernetes-operator)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Quarkus](https://quarkus.io/)


## 📄 License

This project is for demonstration purposes. 