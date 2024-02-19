FROM hms-standalone

RUN apt-get update && apt-get install -y curl rlwrap vim

RUN curl https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.4/hadoop-aws-3.3.4.jar -o /opt/hive-metastore/lib/hadoop-aws-3.3.4.jar && \
    curl https://repo1.maven.org/maven2/software/amazon/awssdk/bundle/2.20.18/bundle-2.20.18.jar -o /opt/hive-metastore/lib/bundle-2.20.18.jar && \
    curl https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.648/aws-java-sdk-bundle-1.12.648.jar -o /opt/hive-metastore/lib/aws-java-sdk-bundle-1.12.648.jar

COPY conf/hive-site.xml /opt/hive-metastore/conf/hive-site.xml

RUN curl https://archive.apache.org/dist/db/derby/db-derby-10.14.2.0/db-derby-10.14.2.0-bin.tar.gz -o db-derby-10.14.2.0-bin.tar.gz && \
    tar xf db-derby-10.14.2.0-bin.tar.gz