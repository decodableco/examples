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

package co.decodable.demos.flinkhelloworld;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSink;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//		env.setParallelism(2);
//		env.enableCheckpointing(5000, CheckpointingMode.EXACTLY_ONCE);

		/*
		// sets the checkpoint storage where checkpoint snapshots will be written
		Configuration config = new Configuration();
		config.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem");
		config.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "s3://flink-data/checkpoints");
		config.set(CheckpointingOptions.SAVEPOINT_DIRECTORY, "s3://flink-data/savepoints");
		config.setString("s3.access.key", "minio");
		config.setString("s3.secret.key", "minio123");
		config.setString("s3.endpoint", "http://localhost:9000");
		config.setString("s3.path.style.access", "true");

		FileSystem.initialize(config, null);

		env.configure(config);
*/

		GeneratorFunction<Long, String> generatorFunction = index -> "Number: " + index;
		long numberOfRecords = 100_000;

		DataGeneratorSource<String> source = new DataGeneratorSource<>(
				generatorFunction,
				numberOfRecords,
				RateLimiterStrategy.perSecond(1),
				Types.STRING);

		DataStreamSource<String> stream =
		        env.fromSource(source,
		        WatermarkStrategy.noWatermarks(),
		        "Generator Source");

		PrintSink<String> sink = new PrintSink<>(true);

		stream.sinkTo(sink);

		env.execute("Flink Java API Skeleton");
	}
}
