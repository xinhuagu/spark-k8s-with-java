package de.berlin.akang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;

/**
 * Main class to launch the Spring Application.
 */
@SpringBootApplication(exclude = {GsonAutoConfiguration.class})
public class SparkApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SparkApplication.class, args);
    }
}