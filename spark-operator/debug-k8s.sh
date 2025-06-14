#!/bin/bash

echo "🔍 Kubernetes Cluster Diagnostics"
echo "=================================="

echo "1. Checking kubectl connection..."
kubectl cluster-info
echo ""

echo "2. Checking current context and namespace..."
kubectl config current-context
kubectl config get-contexts
echo ""

echo "3. Checking if Spark Operator is installed..."
kubectl get crd | grep sparkapplications
echo ""

echo "4. Checking Spark Operator pods..."
kubectl get pods -n spark-operator 2>/dev/null || echo "No spark-operator namespace found"
kubectl get pods -A | grep spark
echo ""

echo "5. Checking RBAC for default service account..."
kubectl get serviceaccount default -n default
kubectl get serviceaccount spark-service-account -n default 2>/dev/null || echo "spark-service-account not found"
echo ""

echo "6. Checking if we can create a simple resource..."
kubectl auth can-i create sparkapplications --as=system:serviceaccount:default:default -n default
echo ""

echo "7. Checking existing SparkApplications..."
kubectl get sparkapplications -n default 2>/dev/null || echo "No SparkApplications found or CRD not installed"
echo ""

echo "8. Checking API resources..."
kubectl api-resources | grep spark
echo ""