package co.decodable.examples.cdc.txbuffering.model;

public record TxMetaDataCustom(
    String id,
    TxStatus status,
    long ts_ms,
    String uuid
) {}
