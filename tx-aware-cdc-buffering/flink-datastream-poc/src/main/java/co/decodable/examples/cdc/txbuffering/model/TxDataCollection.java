package co.decodable.examples.cdc.txbuffering.model;

public record TxDataCollection(
    String data_collection,
    long event_count
) {}
