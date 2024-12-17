package co.decodable.examples.cdc.txbuffering.serde;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;

public class DeserializationSchemaCdcEvents
    implements KafkaRecordDeserializationSchema<CdcEvent> {

    @Override
    public TypeInformation<CdcEvent> getProducedType() {
        return Types.POJO(CdcEvent.class);
    }

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<CdcEvent> out)
            throws IOException {
        out.collect(
            new CdcEvent(
                record.key() != null ? new String(record.key(),StandardCharsets.UTF_8) : null, 
                record.value() != null ? new String(record.value(),StandardCharsets.UTF_8) : null
            )
        );
    }
    
}
