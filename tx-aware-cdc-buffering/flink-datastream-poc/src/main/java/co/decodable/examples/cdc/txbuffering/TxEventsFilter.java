package co.decodable.examples.cdc.txbuffering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.common.functions.FilterFunction;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;

public class TxEventsFilter implements FilterFunction<CdcEvent> {

    private static transient final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String TX_META_DATA_SCHEMA_NAME = "io.debezium.connector.common.TransactionMetadataValue";
    public static final String CDC_EVENT_SCHEMA_NAME_SUFFIX = ".Envelope";

    private final Set<String> tablesOfInterest;

    public TxEventsFilter() {
        this.tablesOfInterest = new HashSet<>();
    }

    public TxEventsFilter(List<String> tablesOfInterest) {
        this.tablesOfInterest = tablesOfInterest.stream()
                .map(t -> t + CDC_EVENT_SCHEMA_NAME_SUFFIX)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean filter(CdcEvent event) {
        try {
            var json = OBJECT_MAPPER.readTree(event.getValue());
            var schemaName = json.get("schema").get("name").asText();
            if (TX_META_DATA_SCHEMA_NAME.equals(schemaName)) {
                var txIdField = json.get("payload").get("id");
                return txIdField.isTextual();
            }
            if (tablesOfInterest.isEmpty()   
                    || tablesOfInterest.contains(schemaName)) {
                var txField = json.get("payload").get("transaction");
                if (!txField.isNull()) {
                    return txField.get("id").isTextual();
                }
            }
            return false;
        } catch (Exception exc) {
            throw new RuntimeException("error: failed to evaluate filter criteria most likely due to wrong event payload shape");
        }
    }

}
