package co.decodable.examples.cdc.txbuffering;

import org.apache.flink.api.java.functions.KeySelector;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;

public class TxIdKeySelector implements KeySelector<CdcEvent,String> {

    private static transient final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getKey(CdcEvent event) {
        try {
            var json = OBJECT_MAPPER.readTree(event.getValue());
            var schemaName = json.get("schema").get("name").asText();
            if (TxEventsFilter.TX_META_DATA_SCHEMA_NAME.equals(schemaName)) {
                return json.get("payload").get("id").asText();
            }
            return json.get("payload").get("transaction").get("id").asText();
        } catch(Exception exc) {
            throw new RuntimeException("error: failed to extract valid TX ID from event");
        }        
    }

}
