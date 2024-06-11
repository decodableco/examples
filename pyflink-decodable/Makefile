.PHONY: clean deploy

TARGET = target
LIBS = $(TARGET)/libs
PYTHON_LIBS = $(TARGET)/python-libs

PY = python3
VENV = $(TARGET)/venv
BIN=$(VENV)/bin

build: $(TARGET) $(LIBS)/flink-sql-connector-kafka-3.0.2-1.18.jar $(LIBS)/flink-python-1.18.1.jar $(TARGET)/pyflink-job.zip $(VENV)

$(TARGET):
	mkdir $(TARGET)

$(VENV): requirements.txt
	$(PY) -m venv $(VENV)
	$(BIN)/pip install -r requirements.txt --target=${PYTHON_LIBS}
	touch $(VENV)

$(LIBS)/flink-sql-connector-kafka-3.0.2-1.18.jar: | $(TARGET)
	mkdir -p $(LIBS)
	wget -N -P $(LIBS) https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/3.0.2-1.18/flink-sql-connector-kafka-3.0.2-1.18.jar

$(LIBS)/flink-python-1.18.1.jar: | $(TARGET)
	mkdir -p $(LIBS)
	wget -N -P $(LIBS) https://repo1.maven.org/maven2/org/apache/flink/flink-python/1.18.1/flink-python-1.18.1.jar

$(TARGET)/pyflink-job.zip: main.py $(LIBS)/flink-sql-connector-kafka-3.0.2-1.18.jar $(LIBS)/flink-python-1.18.1.jar $(VENV)
	cp main.py $(TARGET)
	cd $(TARGET) && zip -r pyflink-job.zip main.py libs python-libs

clean:
	@rm -rf $(TARGET)

deploy: build
	decodable apply decodable-resources.yaml
