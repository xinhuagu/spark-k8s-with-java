package com.example.sparkoperator;

import java.util.HashMap;
import java.util.Map;

public class SparkApplicationManifest {

  public static Map<String, Object> create() {
    Map<String, Object> manifest = new HashMap<>();

    // API version and kind
    manifest.put("apiVersion", "spark.apache.org/v1beta1");
    manifest.put("kind", "SparkApplication");

    // Metadata
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("name", "demo");
    metadata.put("namespace", "default");
    manifest.put("metadata", metadata);

    // Spec - Apache Spark Operator schema
    Map<String, Object> spec = new HashMap<>();

    // Required field for Apache Spark Operator
    Map<String, String> runtimeVersions = new HashMap<>();
    runtimeVersions.put("sparkVersion", "3.5.4");
    spec.put("runtimeVersions", runtimeVersions);

    // Main class
    spec.put("mainClass", "de.berlin.akang.SparkApplication");

    // JAR files
    spec.put("jars", "local:///opt/spark/work/demo.jar");

    // Deployment mode
    spec.put("deploymentMode", "ClusterMode");

    // Spark configuration
    Map<String, String> sparkConf = new HashMap<>();
    sparkConf.put("spark.driver.cores", "1");
    sparkConf.put("spark.driver.memory", "1g");
    sparkConf.put("spark.executor.cores", "1");
    sparkConf.put("spark.executor.memory", "1g");
    sparkConf.put("spark.executor.instances", "2");
    sparkConf.put("spark.kubernetes.container.image", "xinhua/spark-app:v4");
    sparkConf.put("spark.kubernetes.authenticate.driver.serviceAccountName", "default");
    spec.put("sparkConf", sparkConf);

    manifest.put("spec", spec);

    return manifest;
  }

}
