package co.decodable.examples.cdc.txbuffering.serde;

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.shaded.curator5.com.google.common.base.Charsets;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.decodable.examples.cdc.txbuffering.model.TxBuffer;

public class SerializationSchemaTxBuffer
    implements KafkaRecordSerializationSchema<TxBuffer> {

    private static transient final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private String topic;
    
    public SerializationSchemaTxBuffer(String topic) {
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(
            TxBuffer record, KafkaSinkContext context, Long timestamp) {
        try {
            return new ProducerRecord<>(
                    topic,
                    0,
                    record.getBeginMarker().id().getBytes(Charsets.UTF_8),
                    OBJECT_MAPPER.writeValueAsBytes(record));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to serialize record: " + record, e);
        }
    }
}
