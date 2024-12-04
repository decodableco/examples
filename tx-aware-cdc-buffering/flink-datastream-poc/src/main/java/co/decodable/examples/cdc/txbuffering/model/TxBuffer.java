package co.decodable.examples.cdc.txbuffering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.decodable.examples.cdc.txbuffering.CdcTxBuffer;

public class TxBuffer {

    private TxMetaData beginMarker;
    private TxMetaData endMarker;

    private Map<String,List<CdcEvent>> buffer;

    public TxBuffer() {
    }

    public TxMetaData getBeginMarker() {
        return beginMarker;
    }

    public void setBeginMarker(TxMetaData beginMarker) {
        this.beginMarker = beginMarker;
    }

    public TxMetaData getEndMarker() {
        return endMarker;
    }

    public void setEndMarker(TxMetaData endMarker) {
        this.endMarker = endMarker;
    }

    public Map<String, List<CdcEvent>> getBuffer() {
        return buffer;
    }

    public void setBuffer(Map<String, List<CdcEvent>> buffer) {
        this.buffer = buffer;
    }
    
    public static TxBuffer fromTransactionBuffer(CdcTxBuffer txBuffer) throws Exception {
        TxBuffer result = new TxBuffer();
        result.setBeginMarker(txBuffer.getBeginMarker());
        result.setEndMarker(txBuffer.getEndMarker());
        Map<String,List<CdcEvent>> buffer = new HashMap<>();
        for(var table : txBuffer.getBuffer().entrySet()) {
            var cdcEventList = new ArrayList<CdcEvent>();
            var numEvents = table.getValue().size();
            //NOTE: ordinal index starting with 1
            for (int e = 1; e <= numEvents; e++) {
                cdcEventList.add(new CdcEvent(table.getValue().get(e).getKey(),table.getValue().get(e).getValue()));
            }
            buffer.put(table.getKey(), cdcEventList);
        }
        result.setBuffer(buffer);
        return result;
    }

}
