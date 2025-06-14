#!/bin/bash

set -e

echo "Building Docker image and starting Spark orchestrator..."

# Navigate to spark-orchestrator directory
cd spark-orchestrator

# Build the Docker image using Dockerfile.jvm
echo "Building Docker image xinhua/spark-app:v4..."
docker build -f src/main/docker/Dockerfile.jvm -t xinhua/spark-app:v4 .

echo "Docker image built successfully!"

# Start the Spark orchestrator server using Maven
echo "Starting Spark orchestrator server..."
./mvnw quarkus:dev

echo "Spark orchestrator server started!"