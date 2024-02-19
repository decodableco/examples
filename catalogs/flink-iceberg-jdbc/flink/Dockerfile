FROM apache/flink:1.18.1-scala_2.12-java11
SHELL ["/bin/bash", "-c"]

WORKDIR /opt/flink

# Pre-seed the SQL history because I'm nice like that
COPY flink-sql-history /root/.flink-sql-history

COPY iceberg_jdbc.sql /opt/flink/iceberg_jdbc.sql

# Enable SQL Client to find the job manager when running it from this image
RUN sed -i "s/jobmanager.rpc.address: localhost/jobmanager.rpc.address: flink-jobmanager/g" ./conf/flink-conf.yaml

# Install required dependencies
RUN apt-get update && \
    apt-get install -y vim lnav

RUN echo "Add Flink S3 Plugin" && \
    mkdir ./plugins/s3-fs-hadoop && \
    cp ./opt/flink-s3-fs-hadoop-1.18.1.jar ./plugins/s3-fs-hadoop/

RUN echo "-> Install JARs: Postgres JDBC driver" && \
    mkdir -p ./lib/jdbc && pushd $_ && \
    curl https://jdbc.postgresql.org/download/postgresql-42.7.1.jar -O && \
    popd

RUN echo "-> Install JARs: Dependencies for Iceberg" && \
    mkdir -p ./lib/iceberg && pushd $_ && \
    curl https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-flink-runtime-1.17/1.4.3/iceberg-flink-runtime-1.17-1.4.3.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-aws-bundle/1.4.3/iceberg-aws-bundle-1.4.3.jar -O && \
    popd

RUN echo "-> Install JARs: AWS / Hadoop S3" && \
    mkdir -p ./lib/aws && pushd $_ && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.4/hadoop-aws-3.3.4.jar -O && \
    curl https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.648/aws-java-sdk-bundle-1.12.648.jar -O && \
    popd

RUN echo "-> Install JARs: Hadoop" && \
    mkdir -p ./lib/hadoop && pushd $_ && \
    curl https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/2.8.3-10.0/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar -O && \
    popd

RUN echo "Purge build artifacts" && \
    apt-get purge -y --auto-remove $build_deps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

CMD ./bin/start-cluster.sh && sleep infinity
