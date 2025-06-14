#!/bin/bash

SA="system:serviceaccount:default:spark-service-account"

echo "Checking Spark ServiceAccount permissions..."

permissions=(
    "create pods"
    "get pods"
    "list pods"
    "delete pods"
    "create services"
    "get services"
    "delete services"
    "create configmaps"
    "get configmaps"
    "list configmaps"
    "get nodes"
    "list nodes"
)

for perm in "${permissions[@]}"; do
    if kubectl auth can-i $perm --as=$SA >/dev/null 2>&1; then
        echo "✅ $perm: YES"
    else
        echo "❌ $perm: NO"
    fi
done
