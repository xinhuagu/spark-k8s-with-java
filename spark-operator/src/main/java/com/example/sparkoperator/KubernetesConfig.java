package com.example.sparkoperator;

import io.kubernetes.client.openapi.ApiClient;

import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KubernetesConfig {


  @Bean
  public ApiClient kubernetesApiClient() {
    try {
      ApiClient apiClient = Config.defaultClient();
      io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
      log.info("Kubernetes API client configured successfully");
      return apiClient;
    } catch (Exception e) {
      log.error("Failed to configure Kubernetes API client", e);
      throw new RuntimeException("Failed to initialize Kubernetes client", e);
    }
  }

  @Bean
  public CustomObjectsApi customObjectsApi(ApiClient apiClient) {
    return new CustomObjectsApi(apiClient);
  }
}