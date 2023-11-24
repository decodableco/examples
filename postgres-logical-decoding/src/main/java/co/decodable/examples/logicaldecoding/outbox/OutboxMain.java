/*
 *  Copyright 2023 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package co.decodable.examples.logicaldecoding.outbox;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.serialization.SerializationSchema.InitializationContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;

import co.decodable.examples.logicaldecoding.common.deserialization.MessageDeserializer;
import co.decodable.examples.logicaldecoding.common.model.ChangeEvent;
import co.decodable.examples.logicaldecoding.common.model.Message;

public class OutboxMain {

    public static void main(String[] args) throws Exception {
        Properties extraProps = new Properties();
        extraProps.put("poll.interval.ms", "100");
        extraProps.put("snapshot.mode", "never");
        extraProps.put("binary.handling.mode", "base64");

        SourceFunction<String> sourceFunction = PostgreSQLSource.<String> builder()
                .hostname("localhost")
                .port(5432)
                .database("demodb")
                .username("postgresuser")
                .password("postgrespw")
                .decodingPluginName("pgoutput")
                .debeziumProperties(extraProps)
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        KafkaSink<ChangeEvent> sink = KafkaSink.<ChangeEvent> builder()
                .setBootstrapServers("localhost:9092")
                .setRecordSerializer(new OutboxSerializer())
                .setProperty("session.timeout.ms", "45000")
                .setProperty("acks", "all")
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        env.addSource(sourceFunction)
                .map(new ChangeEventDeserializer())
                .filter(ce -> ce.getOp().equals("m"))
                .sinkTo(sink);

        env.execute();
    }

    private static class OutboxSerializer implements KafkaRecordSerializationSchema<ChangeEvent> {

        private static final long serialVersionUID = 1L;

        private ObjectMapper mapper;

        @Override
        public ProducerRecord<byte[], byte[]> serialize(ChangeEvent element, KafkaSinkContext context, Long timestamp) {
            try {
                JsonNode content = element.getMessage().getContent();

                ProducerRecord<byte[], byte[]> record = new ProducerRecord<byte[], byte[]>(
                        content.get("aggregate_type").asText(),
                        content.get("aggregate_id").asText().getBytes(StandardCharsets.UTF_8),
                        mapper.writeValueAsBytes(content.get("payload")));
                record.headers().add("message_id", content.get("id").asText().getBytes(StandardCharsets.UTF_8));

                return record;
            }
            catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Couldn't serialize outbox message", e);
            }
        }

        @Override
        public void open(InitializationContext context, KafkaSinkContext sinkContext) throws Exception {
            mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Message.class, new MessageDeserializer());
            mapper.registerModule(module);
        }
    }

    public static class ChangeEventDeserializer extends RichMapFunction<String, ChangeEvent> {

        private static final long serialVersionUID = 1L;

        private transient ObjectMapper mapper;

        public ChangeEventDeserializer() {
        }

        @Override
        public ChangeEvent map(String value) throws Exception {
            return mapper.readValue(value, ChangeEvent.class);
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Message.class, new MessageDeserializer());
            mapper.registerModule(module);
        }
    }
}
