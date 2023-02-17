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
package co.decodable.examples.logicaldecoding.auditing;

import java.util.List;
import java.util.Properties;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;

import co.decodable.examples.logicaldecoding.auditing.model.AuditState;
import co.decodable.examples.logicaldecoding.common.deserialization.MessageDeserializer;
import co.decodable.examples.logicaldecoding.common.model.ChangeEvent;
import co.decodable.examples.logicaldecoding.common.model.Message;

public class AuditMain {

    public static void main(String[] args) throws Exception {
        Properties extraProps = new Properties();
        extraProps.put("poll.interval.ms", "100");
        extraProps.put("snapshot.mode", "never");

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

        // for local development, also start the Flink UI on http://localhost:8081
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration());
        env.setParallelism(2);
        env.getCheckpointConfig().setCheckpointInterval(Time.seconds(5).toMilliseconds());

        env.addSource(sourceFunction).forceNonParallel()
                .flatMap(new AuditMetadataEnrichmentFunction()).forceNonParallel()
                .print();

        env.execute();
    }

    public static class AuditMetadataEnrichmentFunction extends RichFlatMapFunction<String, String> implements CheckpointedFunction {

        private static final long serialVersionUID = 1L;

        private transient ObjectMapper mapper;
        private transient ListState<AuditState> auditState;
        private transient AuditState localAuditState;

        public void flatMap(String value, Collector<String> out) throws Exception {
            ChangeEvent changeEvent = mapper.readValue(value, ChangeEvent.class);
            String op = changeEvent.getOp();
            String txId = changeEvent.getSource().get("txId").asText();

            // decoding message
            if (op.equals("m")) {
                Message message = changeEvent.getMessage();

                // an audit metadata message
                if (message.getPrefix().equals("audit")) {
                    localAuditState = new AuditState(txId, message.getContent());
                    return;
                }
                else {
                    out.collect(value);
                }
            }
            else {
                if (txId != null && localAuditState != null) {
                    if (txId.equals(localAuditState.getTxId())) {
                        changeEvent.setAuditData(localAuditState.getState());
                    }
                    else {
                        localAuditState = null;
                    }
                }

                changeEvent.setTransaction(null);
                out.collect(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(changeEvent));
            }
        }

        @Override
        public void open(Configuration parameters) {
            mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addDeserializer(Message.class, new MessageDeserializer());
            mapper.registerModule(module);
        }

        @Override
        public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
            if (localAuditState == null) {
                auditState.clear();
            }
            else {
                auditState.update(List.of(localAuditState));
            }
        }

        @Override
        public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {
            ListStateDescriptor<AuditState> descriptor = new ListStateDescriptor<>("auditState", AuditState.class);
            auditState = functionInitializationContext.getOperatorStateStore().getListState(descriptor);
            List<AuditState> auditStates = Lists.newArrayList(auditState.get().iterator());
            switch (auditStates.size()) {
                case 0:
                    localAuditState = null;
                    break;
                case 1:
                    localAuditState = auditStates.get(0);
                    break;
                default: // auditStates.size() > 1
                    throw new IllegalStateException("This mapper is supposed to always run with a parallelism of 1");
            }
        }
    }
}
