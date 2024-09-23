FROM docker.io/hpgrahsl/pyflink-base-1.18.0:latest

ARG APP_BASE_DIR=/opt/flink/usrlib
ARG SECRETS_DIR=/opt/pipeline-secrets

RUN wget -P ${APP_BASE_DIR}/libs https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar
RUN wget -P ${APP_BASE_DIR}/libs https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-mysql-cdc/3.1.0/flink-sql-connector-mysql-cdc-3.1.0.jar
RUN wget -P ${APP_BASE_DIR}/libs https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-mongodb/1.2.0-1.18/flink-sql-connector-mongodb-1.2.0-1.18.jar
RUN wget -P ${APP_BASE_DIR}/libs https://repo1.maven.org/maven2/org/apache/flink/flink-python/1.18.1/flink-python-1.18.1.jar

ADD --chown=flink:flink ./model/cache.tar.gz /opt/flink/

COPY --chown=flink:flink ./requirements.txt ${APP_BASE_DIR}/requirements.txt
RUN pip3 install -r ${APP_BASE_DIR}/requirements.txt

COPY --chown=flink:flink ./.secrets/mysql_host ${SECRETS_DIR}/vector_ingest_mysql_host
COPY --chown=flink:flink ./.secrets/mysql_port ${SECRETS_DIR}/vector_ingest_mysql_port
COPY --chown=flink:flink ./.secrets/mysql_user ${SECRETS_DIR}/vector_ingest_mysql_user
COPY --chown=flink:flink ./.secrets/mysql_password ${SECRETS_DIR}/vector_ingest_mysql_password
COPY --chown=flink:flink ./.secrets/mongodb_uri ${SECRETS_DIR}/vector_ingest_mongodb_uri
COPY --chown=flink:flink ./.secrets/model_server ${SECRETS_DIR}/vector_ingest_model_server

ADD --chown=flink:flink ./main.py ${APP_BASE_DIR}/main.py
