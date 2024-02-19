FROM apache/flink:1.18.1-scala_2.12-java11

WORKDIR /opt/flink

# Set up Hive config
COPY conf/hive-site.xml ./conf/hive-site.xml
# Pre-seed the SQL history because I'm nice like that
COPY flink-sql-history /root/.flink-sql-history

# Enable SQL Client to find the job manager when running it from this image
RUN sed -i "s/jobmanager.rpc.address: localhost/jobmanager.rpc.address: flink-jobmanager/g" ./conf/flink-conf.yaml

# Install required dependencies
RUN apt-get update && \
    apt-get install -y vim lnav

RUN echo "Add Flink S3 Plugin" && \
    mkdir ./plugins/s3-fs-hadoop && \
    cp ./opt/flink-s3-fs-hadoop-1.18.1.jar ./plugins/s3-fs-hadoop/

RUN echo "-> Install JARs: Flink's Hive connector" && \
    mkdir -p ./lib/hive && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-hive-3.1.3_2.12/1.18.1/flink-sql-connector-hive-3.1.3_2.12-1.18.1.jar -o ./lib/hive/flink-sql-connector-hive-3.1.3_2.12-1.18.1.jar

RUN echo "-> Install JARs: Dependencies for Iceberg" && \
    mkdir -p ./lib/iceberg && \
    curl https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-flink-runtime-1.17/1.4.3/iceberg-flink-runtime-1.17-1.4.3.jar -o ./lib/iceberg/iceberg-flink-runtime-1.17-1.4.3.jar

RUN echo "-> Install JARs: AWS / Hadoop S3" && \
    mkdir -p ./lib/aws && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.4/hadoop-aws-3.3.4.jar -o ./lib/aws/hadoop-aws-3.3.4.jar && \
    curl https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.648/aws-java-sdk-bundle-1.12.648.jar -o ./lib/aws/aws-java-sdk-bundle-1.12.648.jar

RUN echo "-> Install JARs: Hadoop" && \
    mkdir -p ./lib/hadoop && \
    curl https://repo1.maven.org/maven2/org/apache/commons/commons-configuration2/2.1.1/commons-configuration2-2.1.1.jar -o ./lib/hadoop/commons-configuration2-2.1.1.jar && \
    curl https://repo1.maven.org/maven2/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar -o ./lib/hadoop/commons-logging-1.1.3.jar && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-auth/3.3.4/hadoop-auth-3.3.4.jar -o ./lib/hadoop/hadoop-auth-3.3.4.jar && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-common/3.3.4/hadoop-common-3.3.4.jar -o ./lib/hadoop/hadoop-common-3.3.4.jar && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/thirdparty/hadoop-shaded-guava/1.1.1/hadoop-shaded-guava-1.1.1.jar -o ./lib/hadoop/hadoop-shaded-guava-1.1.1.jar && \
    curl https://repo1.maven.org/maven2/org/codehaus/woodstox/stax2-api/4.2.1/stax2-api-4.2.1.jar -o ./lib/hadoop/stax2-api-4.2.1.jar && \
    curl https://repo1.maven.org/maven2/com/fasterxml/woodstox/woodstox-core/5.3.0/woodstox-core-5.3.0.jar -o ./lib/hadoop/woodstox-core-5.3.0.jar && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-hdfs-client/3.3.4/hadoop-hdfs-client-3.3.4.jar -o ./lib/hadoop/hadoop-hdfs-client-3.3.4.jar && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-mapreduce-client-core/3.3.4/hadoop-mapreduce-client-core-3.3.4.jar -o ./lib/hadoop/hadoop-mapreduce-client-core-3.3.4.jar

RUN echo "Purge build artifacts" && \
    apt-get purge -y --auto-remove $build_deps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

CMD ./bin/start-cluster.sh && sleep infinity