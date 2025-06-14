package com.example.sparkoperator.service;

import com.example.sparkoperator.SparkApplicationManifest;
import com.example.sparkoperator.model.SparkApplicationResponse;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;


@Slf4j
@Service
public class SparkOperatorService {


  private static final String GROUP = "spark.apache.org";
  private static final String VERSION = "v1beta1";
  private static final String PLURAL = "sparkapplications";

  private final CustomObjectsApi customObjectsApi;
  private final ApiClient apiClient;

  public  SparkOperatorService() {
    try {
      // Configure Kubernetes client
      this.apiClient = Config.defaultClient();
      Configuration.setDefaultApiClient(apiClient);
      this.customObjectsApi = new CustomObjectsApi(apiClient);

      log.info("SparkOperatorService initialized successfully");
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Failed to initialize SparkOperatorService");
      throw new RuntimeException("Failed to initialize Kubernetes client", e);
    }
  }

  public SparkApplicationResponse submitSparkApplication() {
     String jobId = generateJobId();
     String name =  "demo";
     String sparkAppName = name + "-" + jobId;

    log.info("Submitting SparkApplication name: {} jobId: {}", name, jobId);

    try {
      customObjectsApi.createNamespacedCustomObject(
          GROUP,
          VERSION,
          "default",
          PLURAL, SparkApplicationManifest.create(name, jobId),
          "true",
          null,
          null
      );

      log.info("SparkApplication created successfully");

      return new SparkApplicationResponse(
          jobId,
          "SUBMITTED",
          "SparkApplication created successfully",
          sparkAppName
      );

    } catch (ApiException e) {
      log.error("Failed to create SparkApplication: {}, Response: {}",
          sparkAppName, e.getResponseBody());
      return new SparkApplicationResponse(
          jobId,
          "FAILED",
          "API Error: " + e.getMessage() + " - " + e.getResponseBody(),
          sparkAppName
      );
    } catch (Exception e) {
      log.error("Unexpected error creating SparkApplication: {}", sparkAppName);
      return new SparkApplicationResponse(
          jobId,
          "FAILED",
          "Unexpected error: " + e.getMessage(),
          sparkAppName
      );
    }
  }

  public SparkApplicationResponse getSparkApplicationStatus(String jobId) {
    try {
      String sparkAppName = findSparkAppNameByJobId(jobId);
      if (sparkAppName == null) {
        return new SparkApplicationResponse(jobId, "NOT_FOUND", "Job not found", null);
      }

      Object result = customObjectsApi.getNamespacedCustomObject(
          GROUP, VERSION, "default", PLURAL, sparkAppName
      );

      @SuppressWarnings("unchecked")
      Map<String, Object> sparkApp = (Map<String, Object>) result;

      String status = extractStatus(sparkApp);
      String message = extractMessage(sparkApp);

      return new SparkApplicationResponse(jobId, status, message, sparkAppName);

    } catch (ApiException e) {
      log.error("Failed to get SparkApplication status for job: {}", jobId);
      return new SparkApplicationResponse(jobId, "ERROR", "Failed to get status: " + e.getMessage(), null);
    } catch (Exception e) {
      log.error( "Unexpected error getting status for job: {}", jobId);
      return new SparkApplicationResponse(jobId, "ERROR", "Unexpected error: " + e.getMessage(), null);
    }
  }

  public boolean deleteSparkApplication(String jobId) {
    try {
      String sparkAppName = findSparkAppNameByJobId(jobId);
      if (sparkAppName == null) {
        log.warn("SparkApplication not found for job ID: {}", jobId);
        return false;
      }

     customObjectsApi.deleteNamespacedCustomObject(
          GROUP, VERSION, "default", PLURAL, sparkAppName,
          null, null, null, null, null
      );

      log.info("SparkApplication deleted: {}", sparkAppName);
      return true;

    } catch (ApiException e) {
      log.error("Failed to delete SparkApplication for job: {}", jobId);
      return false;
    } catch (Exception e) {
      log.error( "Unexpected error deleting job: {}", jobId);
      return false;
    }
  }



  private String findSparkAppNameByJobId(String jobId) {
    try {
      Object result = customObjectsApi.listNamespacedCustomObject(
          GROUP, VERSION, "default", PLURAL,
          "true",  // pretty
          null,    // allowWatchBookmarks
          null,    // _continue
          null,    // fieldSelector
          "job-id=" + jobId,  // labelSelector
          null,    // limit
          null,    // resourceVersion
          null,    // resourceVersionMatch
          null,    // timeoutSeconds
          false    // watch
      );

      @SuppressWarnings("unchecked")
      Map<String, Object> response = (Map<String, Object>) result;
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

      if (items != null && !items.isEmpty()) {
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) items.get(0).get("metadata");
        return (String) metadata.get("name");
      }

      return null;

    } catch (Exception e) {
      log.error("Error finding SparkApplication by job ID: {}", jobId);
      return null;
    }
  }

  private String extractStatus(Map<String, Object> sparkApp) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> status = (Map<String, Object>) sparkApp.get("status");
      if (status != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> currentState = (Map<String, Object>) status.get("currentState");
        if (currentState != null) {
          return (String) currentState.get("currentStateSummary");
        }
      }
      return "UNKNOWN";
    } catch (Exception e) {
      log.warn("Error extracting status: {}", e.getMessage());
      return "UNKNOWN";
    }
  }

  private String extractMessage(Map<String, Object> sparkApp) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> status = (Map<String, Object>) sparkApp.get("status");
      if (status != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> currentState = (Map<String, Object>) status.get("currentState");
        if (currentState != null) {
          String message = (String) currentState.get("message");
          return message != null ? message : "No message available";
        }
      }
      return "No status available";
    } catch (Exception e) {
      log.warn("Error extracting message: {}", e.getMessage());
      return "No status available";
    }
  }

  private String generateJobId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }


}