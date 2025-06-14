#!/bin/bash

set -e

echo "Installing Spark infrastructure..."

# Apply permissions first (required for Spark to work)
echo "Applying Kubernetes permissions..."
kubectl apply -f infra/k8s-permission/

# Apply database infrastructure
echo "Applying database infrastructure..."
kubectl apply -f infra/k8s-db/

# Apply Spark history server
echo "Applying Spark history server..."
kubectl apply -f infra/k8s-history-server/

echo "Infrastructure installation complete!"
echo "Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod --all --timeout=300s

echo "All infrastructure components are ready!"