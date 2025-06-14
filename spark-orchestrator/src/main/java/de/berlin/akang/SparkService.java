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
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.launcher.SparkLauncher;

@Slf4j
@ApplicationScoped
public class SparkService {

  private final AtomicReference<SparkJobStatus> state = new AtomicReference<>(
      SparkJobStatus.RUNNING);

  @Inject
  SparkAppProperties sparkAppProperties;


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
      log.error("Spark application failed with exit code: {}", exitCode);
      return SparkJobStatus.COMPLETED.equals(state.get());
    }
    return true;
  }

  private SparkLauncher createSparkLauncher(String appName) {
    var jvmFlags = """
        -Dio.netty.tryReflectionSetAccessible=true
        """;
    // @formatter:off
    SparkLauncher launcher = new SparkLauncher().setAppName(appName)
        .setAppResource("local:///opt/spark/work/" + appName + ".jar")
        .setMaster("k8s://" + sparkAppProperties.master())
        .setDeployMode("cluster")
        .setMainClass(sparkAppProperties.mainClass())
        .setVerbose(true)
        .setConf("spark.driver.extraJavaOptions", jvmFlags)

        .setConf("spark.executor.extraClassPath", "local:///opt/spark/work/" + appName + ".jar")
        .setConf("spark.network.timeout", "300")
        .setConf("spark.executor.instances", sparkAppProperties.executor().instances())
        .setConf("spark.executor.cores", sparkAppProperties.executor().cores())
        .setConf("spark.executor.memory", sparkAppProperties.executor().memory())
        .setConf("spark.driver.cores", sparkAppProperties.driver().cores())
        .setConf("spark.driver.memory", sparkAppProperties.driver().memory())
        .setConf("spark.driver.maxResultSize", sparkAppProperties.driver().maxResultSize())
        .setConf("spark.driver.log.level", "ERROR")
        .setConf("spark.executor.log.level", "ERROR")
        .setConf("spark.kubernetes.namespace", "default")
        .setConf("spark.kubernetes.container.image.pullPolicy", "IfNotPresent")
        .setConf("spark.kubernetes.container.image", sparkAppProperties.image().name() + ":" + sparkAppProperties.image().tag())
        .setConf("spark.kubernetes.authenticate.driver.serviceAccountName", "spark-service-account")
        .setConf("spark.kubernetes.authenticate.executor.serviceAccountName", "spark-service-account")
        .setConf("spark.io.compression.codec", "snappy")
        .setConf("spark.sql.sources.partitionOverwriteMode", "dynamic")
        .setConf("spark.databricks.delta.optimizeWrite.enabled", "true")
        .setConf("spark.delta.autoOptimize.autoCompact.enabled", "true");
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