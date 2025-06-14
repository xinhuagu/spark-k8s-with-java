FROM ubuntu:22.04

# Install Java 17, Python3, and other dependencies
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk python3 python3-pip curl wget tar && \
    apt-get clean

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$PATH:$JAVA_HOME/bin

# Install Spark 3.5.4
ENV SPARK_VERSION=3.5.4
ENV HADOOP_VERSION=3
RUN wget https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
    tar -xzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz -C /opt && \
    mv /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark && \
    rm spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

ENV SPARK_HOME=/opt/spark
ENV PATH=$PATH:$SPARK_HOME/bin

ENV DELTA_VERSION=3.2.1
ENV SCALA_VERSION=2.12

RUN pip3 install delta-spark==${DELTA_VERSION}

RUN rm /opt/spark/jars/HikariCP-2.5.1.jar
COPY target/classes/log4j.properties /opt/spark/work-dir/log4j.properties
COPY target/spark-application-1.0.0-SNAPSHOT.jar /opt/spark/work/demo.jar

# Set default command
CMD ["/bin/bash"]