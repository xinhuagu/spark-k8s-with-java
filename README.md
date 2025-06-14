# Spark Demo Application

A Java microservice-based Apache Spark application demonstrating distributed computing capabilities on Kubernetes. This project consists of a Spark orchestrator microservice that triggers Spark jobs on a local Kubernetes cluster.

## Architecture Overview

The application is built using:
- **Spark Orchestrator**: A Quarkus-based microservice that manages Spark job execution
- **Spark Application**: The actual Spark job implementation
- **Kubernetes Infrastructure**: Database, Spark History Server, and RBAC configurations

## Prerequisites

Before running this application, ensure you have the following installed:

### Required Software
- Java 17 or higher
- Maven 3.6+
- Local Kubernetes cluster
- kubectl CLI tool
- Apache Spark 3.5.4

### Local Kubernetes Setup

1. **Start a local Kubernetes cluster with kind or minikube**
   ```bash
   # Using kind
   kind create cluster

   # Or using minikube
   minikube start
   ```

### Apache Spark Installation

1. **Download and Install Spark 3.5.4**
   ```bash
   # Download Spark 3.5.4
   wget https://archive.apache.org/dist/spark/spark-3.5.4/spark-3.5.4-bin-hadoop3.tgz
   
   # Extract to your preferred location
   tar -xzf spark-3.5.4-bin-hadoop3.tgz
   sudo mv spark-3.5.4-bin-hadoop3 /opt/spark
   ```

2. **Set Environment Variables**
   ```bash
   # Add to your ~/.bashrc or ~/.zshrc
   export SPARK_HOME=/opt/spark
   export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
   
   # Reload your shell configuration
   source ~/.bashrc  # or source ~/.zshrc
   ```

3. **Verify Spark Installation**
   ```bash
   spark-submit --version
   ```

## Quick Start

### 1. Install Infrastructure

Run the infrastructure installation script to set up the required Kubernetes resources:

```bash
./install-infra.sh
```

This script will:
- Apply Kubernetes RBAC permissions for Spark
- Deploy PostgreSQL database with persistent storage
- Deploy Spark History Server
- Wait for all pods to be ready

### 2. Build and Start the Application

Execute the build and start script:

```bash
./build-and-start.sh
```

This script will:
- Build the Docker image `xinhua/spark-app:v4` using the Quarkus JVM Dockerfile
- Start the Spark orchestrator microservice in development mode
- The service will be available at `http://localhost:8080`

### 3. Test the Application

Once the orchestrator is running, trigger a Spark job:

```bash
curl -X GET http://localhost:8080/spark
```

This endpoint will:
- Submit a Spark job to the local Kubernetes cluster
- Execute the job using the configured Spark application
- Return the job execution status and results

## Project Structure

```
spark-demo/
├── infra/                          # Kubernetes infrastructure
│   ├── k8s-permission/            # Spark RBAC configurations
│   ├── k8s-db/                    # PostgreSQL database setup
│   └── k8s-history-server/        # Spark History Server
├── spark-application/             # Spark job implementation
│   └── src/main/java/             # Java source code
├── spark-orchestrator/            # Microservice orchestrator
│   ├── src/main/docker/           # Docker configurations
│   └── src/main/java/             # Quarkus application
├── install-infra.sh               # Infrastructure setup script
├── build-and-start.sh             # Build and start script
└── README.md                      # This file
```

## Development Workflow

1. **Make changes** to the Spark application or orchestrator
2. **Stop the running service** (Ctrl+C)
3. **Rebuild and restart** using `./build-and-start.sh`
4. **Test changes** using the curl command

## Monitoring and Debugging

### Kubernetes Resources
```bash
# Check pod status
kubectl get pods

# View pod logs
kubectl logs <pod-name>

# Check services
kubectl get services
```

### Spark History Server
Access the Spark History Server UI at `http://localhost:18080` (if port-forwarded) to monitor job execution.

### Application Logs
The Quarkus application runs in development mode, providing hot reload and detailed logging in the console.

## Troubleshooting

### Common Issues

1. **Kubernetes not accessible**
   - Ensure Docker Desktop Kubernetes is enabled
   - Verify kubectl context: `kubectl config current-context`

2. **SPARK_HOME not set**
   - Verify environment variable: `echo $SPARK_HOME`
   - Ensure Spark binaries are in PATH: `which spark-submit`

3. **Pods not starting**
   - Check pod status: `kubectl get pods`
   - View detailed events: `kubectl describe pod <pod-name>`

4. **Port conflicts**
   - Ensure port 8080 is available
   - Check for running services: `netstat -an | grep 8080`

### Clean Up

To remove all deployed resources:

```bash
# Remove Kubernetes resources
kubectl delete -f infra/k8s-permission/
kubectl delete -f infra/k8s-db/
kubectl delete -f infra/k8s-history-server/

# Remove Docker images (optional)
docker rmi xinhua/spark-app:v4
```

## Configuration

### Environment Variables
- `SPARK_HOME`: Path to Spark installation
- `KUBECONFIG`: Kubernetes configuration file path (if not using default)

### Application Properties
- Spark orchestrator configuration: `spark-orchestrator/src/main/resources/application.yml`
- Spark application configuration: `spark-application/src/main/resources/application.yaml`


## License

This project is provided as-is for demonstration purposes.