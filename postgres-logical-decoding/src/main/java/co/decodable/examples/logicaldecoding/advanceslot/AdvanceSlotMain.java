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
package co.decodable.examples.logicaldecoding.advanceslot;

import java.util.Properties;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;

public class AdvanceSlotMain {

    public static void main(String[] args) throws Exception {
        Properties extraProps = new Properties();
        extraProps.put("poll.interval.ms", "100");
        extraProps.put("snapshot.mode", "never");

        SourceFunction<String> sourceFunction = PostgreSQLSource.<String> builder()
                .hostname("localhost")
                .port(5432)
                .database("db2")
                .username("postgresuser")
                .password("postgrespw")
                .decodingPluginName("pgoutput")
                .debeziumProperties(extraProps)
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.addSource(sourceFunction)
                .print()
                .setParallelism(1);

        env.enableCheckpointing(10_000L);
        env.execute();
    }
}
