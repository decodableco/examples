FROM apache/flink:1.18.1-scala_2.12-java11
SHELL ["/bin/bash", "-c"]

# Install some useful tools
RUN apt-get update && \
    apt-get install -y neovim tree lnav unzip && \
    apt-get purge -y --auto-remove && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/duckdb/duckdb/releases/download/v1.0.0/duckdb_cli-linux-amd64.zip \
    && unzip duckdb_cli-linux-amd64.zip -d /usr/local/bin \
    && rm duckdb_cli-linux-amd64.zip

USER flink
WORKDIR /opt/flink

COPY --chown=flink conf/hive-site.xml ./conf/hive-site.xml
# COPY --chown=flink conf/log4j.properties ./conf/log4j-console.properties

# Enable SQL Client to find the job manager when running it from this image
RUN sed -i "s/jobmanager.rpc.address: localhost/jobmanager.rpc.address: flink-jobmanager/g" ./conf/flink-conf.yaml

# # Enable this for debug logging
# RUN cat >> ./conf/log4j.properties <<EOF
# logger.fs.name = org.apache.hadoop.fs
# logger.fs.level = TRACE
# logger.fs2.name = org.apache.flink.fs
# logger.fs2.level = TRACE
# logger.aws.name = com.amazonaws
# logger.aws.level = TRACE
# logger.delta.name = io.delta
# logger.delta.level =TRACE
# EOF

# RUN cat >> ./conf/log4j-cli.properties <<EOF
# logger.fs.name = org.apache.hadoop.fs
# logger.fs.level = TRACE
# logger.fs2.name = org.apache.flink.fs
# logger.fs2.level = TRACE
# logger.aws.name = com.amazonaws
# logger.aws.level = TRACE
# logger.delta.name = io.delta
# logger.delta.level =TRACE
# EOF

# Install JARs
# Create necessary directories
RUN mkdir -p ./lib/delta ./lib/kafka ./lib/hive ./lib/hadoop

RUN echo "Add Flink S3 Plugin" && \
    mkdir ./plugins/s3-fs-hadoop && \
    cp ./opt/flink-s3-fs-hadoop-1.18.1.jar ./plugins/s3-fs-hadoop/

# Download and Install JARs

RUN echo "-> Install JARs: Flink's Kafka connector" && \
    mkdir -p ./lib/kafka && pushd $_ && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.1.0-1.18/flink-sql-connector-kafka-3.1.0-1.18.jar -O && \
    popd

RUN echo "-> Install JARs: Flink's Hive connector" && \
    mkdir -p ./lib/hive && pushd $_ && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-hive-3.1.3_2.12/1.18.1/flink-sql-connector-hive-3.1.3_2.12-1.18.1.jar -O && \
    popd

RUN echo "-> Install JARs: Dependencies for Delta Lake" && \
    mkdir -p ./lib/delta && pushd $_ && \
    curl https://repo1.maven.org/maven2/io/delta/delta-flink/3.2.0/delta-flink-3.2.0.jar -O && \
    curl https://repo1.maven.org/maven2/io/delta/delta-standalone_2.12/3.2.0/delta-standalone_2.12-3.2.0.jar -O && \
    curl https://repo1.maven.org/maven2/io/delta/delta-storage/3.2.0/delta-storage-3.2.0.jar -O && \
    curl https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-parquet/1.18.1/flink-sql-parquet-1.18.1.jar -O && \
    curl https://repo1.maven.org/maven2/com/chuusai/shapeless_2.12/2.3.4/shapeless_2.12-2.3.4.jar -O && \
    popd

RUN echo "-> Install JARs: AWS / Hadoop S3" && \
    mkdir -p ./lib/aws && pushd $_ && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.4/hadoop-aws-3.3.4.jar -O && \
    curl https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.648/aws-java-sdk-bundle-1.12.648.jar -O  && \
    popd

RUN echo "-> Install JARs: Hadoop" && \
    mkdir -p ./lib/hadoop && pushd $_ && \
    curl https://repo1.maven.org/maven2/com/google/guava/guava/27.0-jre/guava-27.0-jre.jar -O && \
    curl https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.12.7/jackson-databind-2.12.7.jar -O && \
    curl https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.17.1/jackson-core-2.17.1.jar -O && \
    curl https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.12.7/jackson-annotations-2.12.7.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/commons/commons-configuration2/2.1.1/commons-configuration2-2.1.1.jar -O && \
    curl https://repo1.maven.org/maven2/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-auth/3.3.4/hadoop-auth-3.3.4.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-common/3.3.4/hadoop-common-3.3.4.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/thirdparty/hadoop-shaded-guava/1.1.1/hadoop-shaded-guava-1.1.1.jar -O && \
    curl https://repo1.maven.org/maven2/org/codehaus/woodstox/stax2-api/4.2.1/stax2-api-4.2.1.jar -O && \
    curl https://repo1.maven.org/maven2/com/fasterxml/woodstox/woodstox-core/5.3.0/woodstox-core-5.3.0.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-hdfs-client/3.3.4/hadoop-hdfs-client-3.3.4.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-mapreduce-client-core/3.3.4/hadoop-mapreduce-client-core-3.3.4.jar -O  && \
    popd

# Set the launch command
CMD ./bin/start-cluster.sh && sleep infinity