package de.berlin.akang;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties()
public class ConfigProperties {

    private Spark spark;
    private Dih dih;

    public record Spark(
            String appName,
            boolean local,
            Map<String, String> hadoopProperties,
            Map<String, String> sessionProperties,
            int deltaRetentionHours) {}

    public record Dih(String selfServiceUrl, String s3ReflectorUrl) {}
}
