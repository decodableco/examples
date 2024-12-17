package co.decodable.examples.cdc.txbuffering;

import java.nio.file.FileSystems;
import java.util.List;
import java.util.UUID;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SideOutputDataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.OutputTag;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;
import co.decodable.examples.cdc.txbuffering.model.TxBuffer;
import co.decodable.examples.cdc.txbuffering.serde.DeserializationSchemaCdcEvents;
import co.decodable.examples.cdc.txbuffering.serde.SerializationSchemaTxBuffer;

public class CdcTxBufferingJob {

        public static void main(String[] args) throws Exception {

                String kafkaBootstrapServers = "localhost:9092";
                String txMetaDataTopic = "demodb.transaction";

                List<String> cdcTopicsOfInterest = List.of(
                                txMetaDataTopic,
                                "demodb.inventory.customers",
                                "demodb.inventory.addresses",
                                "demodb.inventory.orders",
                                "demodb.inventory.products",
                                "demodb.inventory.products_on_hand"
                        );

                Configuration conf = new Configuration();                
                conf.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem");
                conf.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "file:///"+FileSystems.getDefault().getPath("").toAbsolutePath()+"/.checkpoints");

                var env = StreamExecutionEnvironment
                                .createLocalEnvironmentWithWebUI(conf)
                                .setParallelism(4)
                                .enableCheckpointing(10_000);
                env.configure(conf);
                
                KafkaSource<String> txMetaTopicSource = KafkaSource.<String>builder()
                                .setBootstrapServers(kafkaBootstrapServers)
                                .setTopics(txMetaDataTopic)
                                .setGroupId("my-cg-1234-bs")
                                .setStartingOffsets(OffsetsInitializer.earliest())
                                .setValueOnlyDeserializer(new SimpleStringSchema())
                                .build();

                KafkaSource<CdcEvent> cdcTopicsSource = KafkaSource.<CdcEvent>builder()
                                .setBootstrapServers(kafkaBootstrapServers)
                                .setTopics(cdcTopicsOfInterest)
                                .setGroupId("my-cg-1234-ks")
                                .setStartingOffsets(OffsetsInitializer.earliest())
                                .setDeserializer(new DeserializationSchemaCdcEvents())
                                .build();
                
                KafkaSink<String> txMetaDataTopicSink = KafkaSink.<String>builder()
                                .setBootstrapServers(kafkaBootstrapServers)
                                .setRecordSerializer(
                                        KafkaRecordSerializationSchema.builder()
                                        .setTopic(txMetaDataTopic)
                                        .setValueSerializationSchema(new SimpleStringSchema())
                                        .build()
                                )
                                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                                .build();

                KafkaSink<TxBuffer> cdcTxBuffersSink = KafkaSink.<TxBuffer>builder()
                                .setBootstrapServers(kafkaBootstrapServers)
                                .setRecordSerializer(new SerializationSchemaTxBuffer("cdc.tx.buffers"))
                                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                                .build();

                BroadcastStream<String> broadcastTxMetaEvents = env.fromSource(
                                txMetaTopicSource,
                                WatermarkStrategy.noWatermarks(),
                                "kafka-source-tx-meta-records")
                                .broadcast(CdcTxBufferingLogic.BROADCAST_STATE_DESCRIPTOR_LATEST_TX_META,
                                        CdcTxBufferingLogic.BROADCAST_STATE_DESCRIPTOR_TX_CHRONO_INDEX,
                                        CdcTxBufferingLogic.BROADCAST_STATE_DESCRIPTOR_TXID_ORDER_LUT,
                                        CdcTxBufferingLogic.BROADCAST_STATE_DESCRIPTOR_ORDER_TXID_LUT);

                KeyedStream<CdcEvent,String> filteredAndKeyedCdcRecords = env.fromSource(
                                cdcTopicsSource,
                                WatermarkStrategy.noWatermarks(),
                                "kafka-source-cdc-records")
                                .filter(new TxEventsFilter(cdcTopicsOfInterest))
                                .keyBy(new TxIdKeySelector());

                final String uniqueProcessingId = UUID.randomUUID().toString();
                SingleOutputStreamOperator<TxBuffer> bufferedCdcEvents = filteredAndKeyedCdcRecords
                                .connect(broadcastTxMetaEvents)
                                .process(new CdcTxBufferingLogic(uniqueProcessingId));
                bufferedCdcEvents.sinkTo(cdcTxBuffersSink);
                
                final OutputTag<String> txBufferingSideOutput = new OutputTag<String>("tx-buffering-side-output") {};
                SideOutputDataStream<String> sideChannelStream = bufferedCdcEvents.getSideOutput(txBufferingSideOutput);
                sideChannelStream.sinkTo(txMetaDataTopicSink);

                env.execute("Experimental TX-aware CDC Event Aggregation");
        }

}
