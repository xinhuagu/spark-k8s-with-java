package com.example.sparkoperator;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import java.util.HashMap;
import java.util.Map;

public class SimpleSubmitter {

  public static void main(String[] args) throws Exception {
    // 1. Initialize Kubernetes client
    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    CustomObjectsApi customObjectsApi = new CustomObjectsApi(client);

    // 2. Create SparkApplication manifest
    Map<String, Object> sparkApp = createSparkApplicationManifest();

    // 3. Submit to Kubernetes
    try {
      Object result = customObjectsApi.createNamespacedCustomObject(
          "spark.apache.org",      // group
          "v1beta1",               // version
          "default",               // namespace
          "sparkapplications",     // plural
          sparkApp,                // body
          "true",                  // pretty
          null,                    // dryRun
          null                     // fieldManager
      );

      System.out.println("✅ Spark job submitted successfully!");
      System.out.println("Result: " + result);

    } catch (Exception e) {
      System.err.println("❌ Failed to submit Spark job: ");
      System.err.println("Error message: " + e.getMessage());
      
      if (e instanceof io.kubernetes.client.openapi.ApiException) {
        io.kubernetes.client.openapi.ApiException apiEx = (io.kubernetes.client.openapi.ApiException) e;
        System.err.println("HTTP Status Code: " + apiEx.getCode());
        System.err.println("Response Body: " + apiEx.getResponseBody());
        System.err.println("Response Headers: " + apiEx.getResponseHeaders());
      }
      e.printStackTrace();
    }
  }

  private static Map<String, Object> createSparkApplicationManifest() {
    Map<String, Object> manifest = new HashMap<>();

    // API version and kind
    manifest.put("apiVersion", "spark.apache.org/v1beta1");
    manifest.put("kind", "SparkApplication");

    // Metadata
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("name", "demo1");
    metadata.put("namespace", "default");
    manifest.put("metadata", metadata);
    manifest.put("annotations", Map.of(
        "sparkoperator.k8s.io/ttl-seconds-after-finished", "-1"
    ));

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
