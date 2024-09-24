FROM ghcr.io/criccomini/hive-metastore-standalone:0.3.1

RUN apt-get update && apt-get install -y curl rlwrap vim

RUN cd /opt/hive-metastore/lib && \
    curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.4/hadoop-aws-3.3.4.jar -O && \
    curl https://repo1.maven.org/maven2/software/amazon/awssdk/bundle/2.20.18/bundle-2.20.18.jar -O && \
    curl https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.648/aws-java-sdk-bundle-1.12.648.jar -O

COPY conf/hive-site.xml /opt/hive-metastore/conf/hive-site.xml

RUN cd ~ && \
    curl https://archive.apache.org/dist/db/derby/db-derby-10.14.2.0/db-derby-10.14.2.0-bin.tar.gz -o db-derby-10.14.2.0-bin.tar.gz && \
    tar xf db-derby-10.14.2.0-bin.tar.gz
