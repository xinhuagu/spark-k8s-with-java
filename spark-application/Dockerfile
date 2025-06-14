FROM apache/spark:3.5.4-scala2.12-java17-python3-ubuntu


RUN rm /opt/spark/jars/HikariCP-2.5.1.jar
COPY target/classes/log4j.properties /opt/spark/work-dir/log4j.properties
COPY target/spark-application-1.0.0-SNAPSHOT.jar /opt/spark/work/demo.jar
