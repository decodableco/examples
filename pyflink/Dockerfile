FROM decodable-examples/pyflink-base:latest

RUN wget -P /opt/flink/usrlib https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.0.2-1.18/flink-sql-connector-kafka-3.0.2-1.18.jar
ADD --chown=flink:flink pyflink_hello_world.py /opt/flink/usrlib/pyflink_hello_world.py
