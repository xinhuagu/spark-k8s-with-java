package com.example.sparkoperator.controller;


import com.example.sparkoperator.model.SparkApplicationResponse;
import com.example.sparkoperator.service.SparkOperatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
  @RequestMapping("/spark")
  @CrossOrigin(origins = "*")
  public class SparkOperatorController {

    @Autowired
    private SparkOperatorService sparkOperatorService;

    @GetMapping("/submit")
    public ResponseEntity<?> submitSparkApplication() {
      SparkApplicationResponse response = sparkOperatorService.submitSparkApplication();

      if ("FAILED".equals(response.getStatus())) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
      }

      return ResponseEntity.accepted().body(response);
    }


    @GetMapping("/status/{jobId}")
    public ResponseEntity<SparkApplicationResponse> getStatus(@PathVariable String jobId) {
      SparkApplicationResponse response = sparkOperatorService.getSparkApplicationStatus(jobId);

      if ("NOT_FOUND".equals(response.getStatus())) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(response);
    }


  }
