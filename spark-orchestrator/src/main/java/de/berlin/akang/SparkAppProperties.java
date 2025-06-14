package de.berlin.akang;

import io.smallrye.config.ConfigMapping;
import jakarta.inject.Singleton;
import java.util.Map;


@ConfigMapping(prefix = "spark")
@Singleton
public interface SparkAppProperties {
  String master();
  Driver driver();
  Executor executor();
  String mainClass();
  Map<String, String> submit();
  Image image();
  String saName();


  interface Driver {
    String cores();
    String memory();
    String maxResultSize();
  }

  interface Executor {
    String cores();
    String memory();
    String instances();
  }

  interface Image {
    String name();
    String tag();
  }
}

