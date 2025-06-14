package de.berlin.akang;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppStartupRunner implements ApplicationRunner {
    private final SparkService sparkService;

    @Override
    public void run(ApplicationArguments args) {

        try {
            log.info("Starting application");
            try(
                SparkSession session = sparkService.generateSparkSession()
            ) {
                log.info("Spark session started successfully");
                session.sparkContext().setLogLevel("WARN");
                session.catalog();
                session.sql("DROP TABLE IF EXISTS test");

                session.close();

            } catch (Exception e) {
                log.error("Error during Spark session execution: {}", e.getMessage(), e);
                System.exit(1);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }

        System.exit(0);
    }



}
