package de.berlin.akang;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

/**
 * Servie for Spark session management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SparkService {

    private final ConfigProperties configproperties;



    /**
     * Generates a Spark session.
     */
    public SparkSession generateSparkSession() {
        SparkSession.Builder sessionBuilder =
                SparkSession.builder().appName(configproperties.getSpark().appName());

        configproperties.getSpark().sessionProperties().forEach((key, value) -> {
            log.error("setting session property {}, {}", key, value);
            sessionBuilder.config(key, value);
        });

        if (configproperties.getSpark().local()) {
            sessionBuilder.master("local[*]");
        }

        SparkSession session = sessionBuilder.getOrCreate();

        configproperties.getSpark().hadoopProperties().forEach((key, value) -> {
            log.error("setting hadoop property {}, {}", key, value);
            session.sparkContext().hadoopConfiguration().set(key, value);
        });


        session.sparkContext().setLogLevel("ERROR");

        return session;
    }
}
