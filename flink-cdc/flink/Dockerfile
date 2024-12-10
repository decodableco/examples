FROM flink:1.18.1-scala_2.12-java11
SHELL ["/bin/bash", "-c"]


# Install some useful tools
RUN apt-get update && \
    apt-get install -y vim tree lnav unzip

RUN echo "Purge apt artifacts" && \
    apt-get purge -y --auto-remove $build_deps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

USER flink
RUN echo "alias ll='ls -la'" >> ~/.bashrc
RUN echo "execution.checkpointing.interval: 3000" >> ./conf/flink-conf.yaml
WORKDIR /opt/flink

# Enable SQL Client to find the job manager when running it from this image
RUN sed -i "s/jobmanager.rpc.address: localhost/jobmanager.rpc.address: flink-jobmanager/g" ./conf/flink-conf.yaml

# Install JARs for Flink
RUN echo "-> Install MySQL driver JAR" && \
    mkdir -p ./lib/mysql && pushd $_ && \
    curl https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar -O && \
    popd

RUN echo "-> Download JARs" && \
    mkdir -p jars && pushd $_ && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-elasticsearch7/3.0.1-1.17/flink-sql-connector-elasticsearch7-3.0.1-1.17.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-mysql-cdc/3.2.1/flink-sql-connector-mysql-cdc-3.2.1.jar -O && \
    popd

# Install Flink CDC 
RUN curl https://dlcdn.apache.org/flink/flink-cdc-3.2.1/flink-cdc-3.2.1-bin.tar.gz -o /tmp/flink-cdc.tgz && \
    tar xf /tmp/flink-cdc.tgz

# Install pipeline connector JARs
RUN echo "-> Install JARs" && \
    pushd ./flink-cdc-3.2.1/lib && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-cdc-pipeline-connector-mysql/3.2.0/flink-cdc-pipeline-connector-mysql-3.2.0.jar -O && \
    curl https://repo1.maven.org/maven2/org/apache/flink/flink-cdc-pipeline-connector-elasticsearch/3.2.0/flink-cdc-pipeline-connector-elasticsearch-3.2.0.jar -O && \
    curl https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.1.0/mysql-connector-j-9.1.0.jar -O && \
    popd

COPY ../mysql-to-es.yaml ./flink-cdc-3.2.1/
USER root
RUN chown flink ./flink-cdc-3.2.1/mysql-to-es.yaml
USER flink

# Set the launch command
CMD ./bin/start-cluster.sh && sleep infinity
