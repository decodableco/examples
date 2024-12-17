package co.decodable.examples.cdc.txbuffering;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.flink.api.common.typeinfo.TypeInfo;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;
import co.decodable.examples.cdc.txbuffering.model.TxMetaData;
import co.decodable.examples.cdc.txbuffering.typeinfo.TxBufferTypeInfoFactory;

public class CdcTxBuffer {

    @TypeInfo(TxBufferTypeInfoFactory.class)
    private Map<String,Map<Integer,CdcEvent>> buffer = new HashMap<>();
    
    private TxMetaData beginMarker;
    private TxMetaData endMarker;
    private boolean completed;

    public Map<String,Map<Integer,CdcEvent>> getBuffer() {
        return Collections.unmodifiableMap(buffer);
    }

    public void setBuffer(Map<String,Map<Integer,CdcEvent>> buffer) {
        this.buffer = buffer;
    }

    public TxMetaData getBeginMarker() {
        return beginMarker;
    }

    public void setBeginMarker(TxMetaData beginMarker) {
        this.beginMarker = beginMarker;
        checkAndUpdateCompleteness();
    }

    public TxMetaData getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(TxMetaData endMarker) {
        this.endMarker = endMarker;
        checkAndUpdateCompleteness();
    }

    public void putCdcEvent(String dataCollection, int ordinal, CdcEvent event) {
        if(!buffer.containsKey(dataCollection)) {
            var events = new LinkedHashMap<Integer,CdcEvent>();
            events.put(ordinal,event);
            buffer.put(dataCollection,events);
        } else {
            buffer.get(dataCollection).put(ordinal,event);
        }
        checkAndUpdateCompleteness();
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    private boolean checkAndUpdateCompleteness() {
        if (beginMarker == null || endMarker == null) {
            return false;
        }
        for(var dcEntry : endMarker.data_collections()) {
            var expectedEventCount = dcEntry.event_count();
            if (!buffer.containsKey(dcEntry.data_collection())
                || buffer.get(dcEntry.data_collection()).size() != expectedEventCount) {   
                return false;
            }
        }
        completed = true;
        return true;
    }

}
