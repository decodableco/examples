/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.decodable.demos.arrayagg;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class DataStreamJob {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.enableCheckpointing(5000L);
		
		TableEnvironment tableEnv = StreamTableEnvironment.create(env);
		Configuration configuration = tableEnv.getConfig().getConfiguration();
		// set low-level key-value options
		configuration.setString("table.exec.mini-batch.enabled", "true"); // enable mini-batch optimization
		configuration.setString("table.exec.mini-batch.allow-latency", "1 s"); // use 5 seconds to buffer input records
		configuration.setString("table.exec.mini-batch.size", "1000");
		// Create a source table
//		tableEnv.createTemporaryTable("SourceTable", TableDescriptor.forConnector("datagen")
//		    .schema(Schema.newBuilder()
//		      .column("f0", DataTypes.STRING())
//		      .build())
//		    .option(DataGenConnectorOptions.ROWS_PER_SECOND, 1L)
//		    .build());

		tableEnv.createTemporarySystemFunction("array_aggr", ArrayAggr.class);
		
		tableEnv.executeSql("CREATE TABLE purchase_orders (\n"
				+ "   id INT,\n"
				+ "   order_date DATE,\n"
				+ "   purchaser_id INT,\n"
				+ "   db_name STRING METADATA FROM 'database_name' VIRTUAL,\n"
				+ "   operation_ts TIMESTAMP_LTZ(3) METADATA FROM 'op_ts' VIRTUAL,\n"
				+ "   PRIMARY KEY (id) NOT ENFORCED\n"
				+ ")\n"
				+ "WITH (\n"
				+ "   'connector' = 'postgres-cdc',\n"
				+ "   'hostname' = 'localhost',\n"
				+ "   'port' = '5432',\n"
				+ "   'username' = 'postgres',\n"
				+ "   'password' = 'postgres',\n"
				+ "   'database-name' = 'postgres',\n"
				+ "   'schema-name' = 'inventory',\n"
				+ "   'table-name' = 'purchase_orders',\n"
				+ "   'slot.name' = 'purchase_orders_slot',\n"
				+ "   'debezium.database.server.name' = 'dbserver1'\n"
				+ " );"
			);
		
		tableEnv.executeSql("CREATE TABLE order_lines (\n"
				+ "   id INT,\n"
				+ "   order_id INT,\n"
				+ "   product_id INT,\n"
				+ "   quantity INT,\n"
				+ "   price DOUBLE,\n"
				+ "   PRIMARY KEY (id) NOT ENFORCED\n"
				+ ")\n"
				+ "WITH (\n"
				+ "   'connector' = 'postgres-cdc',\n"
				+ "   'hostname' = 'localhost',\n"
				+ "   'port' = '5432',\n"
				+ "   'username' = 'postgres',\n"
				+ "   'password' = 'postgres',\n"
				+ "   'database-name' = 'postgres',\n"
				+ "   'schema-name' = 'inventory',\n"
				+ "   'table-name' = 'order_lines',\n"
				+ "   'slot.name' = 'order_lines_slot',\n"
				+ "   'debezium.database.server.name' = 'dbserver2'\n"
				+ " );"
			);
		
		// Create a sink table (using SQL DDL)
		//tableEnv.executeSql("CREATE TABLE OrdersWithLines WITH ('connector' = 'print') "
		tableEnv.executeSql("CREATE TABLE orders_with_lines_kafka (\n"
				+ "  order_id INT,\n"
				+ "  order_date DATE,\n"
				+ "  purchaser_id INT,\n"
				+ "  lines ARRAY<ROW<id INT, product_id INT, quantity INT, price DOUBLE>>,\n"
				+ "  PRIMARY KEY (order_id) NOT ENFORCED\n"
				+ " )\n"
				+ "WITH (\n"
				+ "    'connector' = 'upsert-kafka',\n"
				+ "    'topic' = 'purchase-orders-processed',\n"
				+ "    'properties.bootstrap.servers' = 'needed-killdeer-12338-eu1-kafka.upstash.io:9092',\n"
				+ "    'properties.security.protocol' = 'SASL_SSL',\n"
				+ "    'properties.sasl.mechanism' = 'SCRAM-SHA-256',\n"
				+ "    'properties.sasl.jaas.config' = 'org.apache.kafka.common.security.scram.ScramLoginModule required username=\"bmVlZGVkLWtpbGxkZWVyLTEyMzM4JGXDVQICFuw6Xsa_8gQI-YkKrxorVd-BlZU\" password=\"5d04d8aef8b54136bb848db026553338\";',\n"
				+ "    'key.format' = 'json',\n"
				+ "    'value.format' = 'json'"
				+ ")");
		
		//env.createTemporarySystemFunction("SubstringFunction", SubstringFunction.class);
		
//		tableEnv.executeSql("insert into OrdersWithLines SELECT po.order_id, po.customer_name from PurchaseOrders po");
//insert into OrdersWithLines SELECT po.order_id, po.customer_name, collect(table('foo', 'bar')) as lines from P
		//, collect(VALUES('foo'), ('bar')
		// ARRAY[row(10001, 'screws'), row(10002, 'hammer')]
		
//		tableEnv.executeSql("insert into OrdersWithLines SELECT po.order_id, po.customer_name, WeightedAvg(row(li.order_id, li.price, li.quantity)) from PurchaseOrders po left join PurchaseOrderLineItems li on li.order_id = po.order_id "
//				+ "GROUP BY po.order_id, po.customer_name");
		
		tableEnv.executeSql("INSERT INTO orders_with_lines_kafka\n"
				+ "  SELECT\n"
				+ "    po.id,\n"
				+ "    po.order_date,\n"
				+ "    po.purchaser_id,\n"
				+ "    ( SELECT ARRAY_AGGR(ROW(ol.id, ol.product_id, ol.quantity, ol.price))\n"
				+ "      FROM order_lines ol\n"
				+ "      WHERE ol.order_id = po.id )\n"
				+ "  FROM\n"
				+ "    purchase_orders po;");
	}
}
