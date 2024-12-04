package co.decodable.examples.cdc.txbuffering.model;

public class CdcEvent {

    private String key;
    private String value;

    public CdcEvent() {
    }
    
    public CdcEvent(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CdcEvent [key=" + key + ", value=" + value + "]";
    }

}
