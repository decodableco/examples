.PHONY: clean deploy

TARGET = target
LIBS = $(TARGET)/libs
PYTHON_LIBS = $(TARGET)/python-libs

PY = python3
VENV = $(TARGET)/venv
BIN=$(VENV)/bin

build: $(TARGET) $(LIBS)/mysql-connector-java-8.0.30.jar $(LIBS)/flink-sql-connector-mysql-cdc-3.1.0.jar $(LIBS)/flink-sql-connector-mongodb-1.2.0-1.18.jar $(LIBS)/flink-python-1.18.1.jar $(TARGET)/pyflink-job.zip $(VENV)

$(TARGET):
	mkdir $(TARGET)

$(VENV): requirements.txt
	$(PY) -m venv $(VENV)
	$(BIN)/pip install -r requirements.txt --target=${PYTHON_LIBS}
	touch $(VENV)

$(LIBS)/mysql-connector-java-8.0.30.jar: | $(TARGET)
	mkdir -p $(LIBS)
	wget -N -P $(LIBS) https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.30/mysql-connector-java-8.0.30.jar

$(LIBS)/flink-sql-connector-mysql-cdc-3.1.0.jar: | $(TARGET)
	mkdir -p $(LIBS)
	wget -N -P $(LIBS) https://repo1.maven.org/maven2/org/apache/flink/flink-sql-connector-mysql-cdc/3.1.0/flink-sql-connector-mysql-cdc-3.1.0.jar

$(LIBS)/flink-sql-connector-mongodb-1.2.0-1.18.jar: | $(TARGET)
	mkdir -p $(LIBS)
	wget -N -P $(LIBS) https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-mongodb/1.2.0-1.18/flink-sql-connector-mongodb-1.2.0-1.18.jar

$(LIBS)/flink-python-1.18.1.jar: | $(TARGET)
	mkdir -p $(LIBS)
	wget -N -P $(LIBS) https://repo1.maven.org/maven2/org/apache/flink/flink-python/1.18.1/flink-python-1.18.1.jar

$(TARGET)/pyflink-job.zip: main.py $(LIBS)/mysql-connector-java-8.0.30.jar $(LIBS)/flink-sql-connector-mysql-cdc-3.1.0.jar $(LIBS)/flink-sql-connector-mongodb-1.2.0-1.18.jar $(LIBS)/flink-python-1.18.1.jar $(VENV)
	cp main.py $(TARGET)
	cd $(TARGET) && zip -r pyflink-job.zip main.py libs python-libs

clean:
	@rm -rf $(TARGET)

deploy: build
	decodable apply decodable-resources.yaml
