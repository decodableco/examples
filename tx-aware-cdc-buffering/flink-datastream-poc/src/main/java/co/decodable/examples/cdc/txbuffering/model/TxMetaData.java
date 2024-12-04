package co.decodable.examples.cdc.txbuffering.model;

import java.util.List;

import org.apache.flink.api.common.typeinfo.TypeInfo;

import co.decodable.examples.cdc.txbuffering.typeinfo.TxDataCollectionTypeInfoFactory;

public record TxMetaData(
    String id,
    TxStatus status,
    long event_count,
    @TypeInfo(TxDataCollectionTypeInfoFactory.class)
    List<TxDataCollection> data_collections,
    long ts_ms
) {}
