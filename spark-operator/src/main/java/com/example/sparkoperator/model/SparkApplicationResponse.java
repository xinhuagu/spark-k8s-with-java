package com.example.sparkoperator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class SparkApplicationResponse {

  private String jobId;
  private String status;
  private String message;
  private String sparkAppName;
  private long submissionTime;

  public SparkApplicationResponse(String jobId, String status, String message, String sparkAppName) {
    this.jobId = jobId;
    this.status = status;
    this.message = message;
    this.sparkAppName = sparkAppName;
  }
}