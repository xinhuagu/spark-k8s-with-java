package de.berlin.akang;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;

@Slf4j
@ApplicationScoped
public class SparkService {

  private final AtomicReference<SparkJobStatus> state = new AtomicReference<>(
      SparkJobStatus.RUNNING);

  protected enum SparkJobStatus {
    RUNNING, COMPLETED, ERROR
  }


  protected boolean submitSparkApp(String appName) {
    int exitCode = -1;
    try {
      SparkLauncher launcher = createSparkLauncher(appName);
      log.info("Submitting Spark application {}", appName);

      exitCode = launch(launcher);
    } catch (Exception e) {
      log.error("Error submitting Spark application", e);
    }

    log.info("===================================================");
    log.info("Spark application finished with status:{}", state.get().name());
    log.info("Spark application finished with Container Exit code: {}", exitCode);
    log.info("===================================================");

    if (exitCode == 0) {
      return SparkJobStatus.COMPLETED.equals(state.get());
    }

    log.error("Spark application failed with exit code: {}", exitCode);
    return true;
  }

  private SparkLauncher createSparkLauncher(String appName) {
    var jvmFlags = """
        -Dio.netty.tryReflectionSetAccessible=true
        """;
    // @formatter:off
    SparkLauncher launcher = new SparkLauncher().setAppName(appName)
        .setAppResource("local:///opt/spark/work/demo.jar")
        .setMaster("k8s://https://127.0.0.1:6443")
        .setDeployMode("cluster")
        .setMainClass("de.berlin.akang.SparkApplication")
        .setVerbose(true)
        .setConf("spark.driver.extraJavaOptions", jvmFlags)

        .setConf("spark.executor.extraClassPath", "local:///opt/spark/work/demo.jar")
        .setConf("spark.network.timeout", "300")
        .setConf("spark.executor.instances", "1")
        .setConf("spark.executor.cores", "1")
        .setConf("spark.executor.memory", "1024m")
        .setConf("spark.driver.cores", "1")
        .setConf("spark.driver.memory", "1024m")
        .setConf("spark.driver.log.level", "ERROR")
        .setConf("spark.executor.log.level", "ERROR")
        .setConf("spark.kubernetes.namespace", "default")
        .setConf("spark.kubernetes.container.image.pullPolicy", "IfNotPresent")
        .setConf("spark.kubernetes.container.image", "spark-app:latest")
        .setConf("spark.kubernetes.authenticate.driver.serviceAccountName", "spark-service-account")
        .setConf("spark.kubernetes.authenticate.executor.serviceAccountName", "spark-service-account")
        .setConf("spark.io.compression.codec", "snappy")
        .setConf("spark.sql.sources.partitionOverwriteMode", "dynamic")
        .setConf("spark.databricks.delta.optimizeWrite.enabled", "true")
        .setConf("spark.delta.autoOptimize.autoCompact.enabled", "true")
        .setConf("spark.kubernetes.executor.deleteOnTermination", "false")
        .setConf("spark.kubernetes.driver.deleteOnTermination", "false")
        .setConf("spark.kubernetes.local.dirs.tmpfs", "true")
        .setConf("spark.eventLog.enabled", "true")
        .setConf("spark.eventLog.dir", "/mnt/spark-history")
        .setConf("spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-history-pvc.options.claimName", "spark-history-pvc")
        .setConf("spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-history-pvc.mount.path", "/mnt/spark-history")
        .setConf("spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-history-pvc.options.claimName", "spark-history-pvc")
        .setConf("spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-history-pvc.mount.path", "/mnt/spark-history");
    // @formatter:on

    return launcher;
  }


  protected int launch(final SparkLauncher sparkLauncher) throws InterruptedException, IOException {
    final Process process = sparkLauncher.launch();
    try (ExecutorService executorService = Executors.newFixedThreadPool(2)) {
      executorService.execute(streamProcessOutput(process.getInputStream(), "OUTPUT"));
      executorService.execute(streamProcessOutput(process.getErrorStream(), "SPARK"));
      return process.waitFor();
    }
  }

  protected Runnable streamProcessOutput(InputStream inputStream, String type) {
    return () -> {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (line.contains("termination reason: Error")) {
            state.set(SparkJobStatus.ERROR);
            return;
          }
          if (line.contains("termination reason: Completed")) {
            state.set(SparkJobStatus.COMPLETED);
            return;
          }
          log.info("[{}] {}", type, line);
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    };
  }
}