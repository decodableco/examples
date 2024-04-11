FROM apache/flink:1.18.1-scala_2.12-java11
SHELL ["/bin/bash", "-c"]

# Install some useful tools
RUN apt-get update && \
    apt-get install -y neovim tree lnav unzip

RUN echo "Purge apt artifacts" && \
    apt-get purge -y --auto-remove $build_deps && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

USER flink

WORKDIR /opt/flink

# Enable SQL Client to find the job manager when running it from this image
RUN sed -i "s/jobmanager.rpc.address: localhost/jobmanager.rpc.address: flink-jobmanager/g" ./conf/flink-conf.yaml

# Install JARs

RUN echo "-> Install JARs: Flink's JDBC connector and Postgres JDBC Driver" && \
    mkdir -p ./lib/jdbc && pushd $_ && \
    curl https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc/3.1.2-1.18/flink-connector-jdbc-3.1.2-1.18.jar -O && \
    curl https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar -O && \
    popd

# Set the launch command
CMD ./bin/start-cluster.sh && sleep infinity
