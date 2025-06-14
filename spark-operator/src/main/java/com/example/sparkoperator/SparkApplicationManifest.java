package com.example.sparkoperator;

import java.util.HashMap;
import java.util.Map;

public class SparkApplicationManifest {

  public static Map<String, Object> create(String name, String jobId) {
    var appName = name + "-" + jobId;

    Map<String, Object> manifest = new HashMap<>();

    // API version and kind
    manifest.put("apiVersion", "spark.apache.org/v1beta1");
    manifest.put("kind", "SparkApplication");

    // Metadata
    Map<String, Object> metadata = new HashMap<>();
    manifest.put("metadata", metadata);

    metadata.put("name", appName);
    metadata.put("namespace", "default");

    // labels
    Map<String, String> labels = new HashMap<>();
    labels.put("app", "spark-operator");
    labels.put("job-id", jobId);
    labels.put("app-name", "spark-operator" + appName);
    metadata.put("labels", labels);

    // Spec - Apache Spark Operator schema
    Map<String, Object> spec = new HashMap<>();
    Map<String, String> runtimeVersions = new HashMap<>();
    runtimeVersions.put("sparkVersion", "3.5.4");
    spec.put("runtimeVersions", runtimeVersions);
    spec.put("ttlSecondsAfterFinished", 3600L);

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
