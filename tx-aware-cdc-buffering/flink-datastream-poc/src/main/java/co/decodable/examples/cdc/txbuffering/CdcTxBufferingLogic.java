package co.decodable.examples.cdc.txbuffering;

import java.io.IOException;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;
import co.decodable.examples.cdc.txbuffering.model.TxBuffer;
import co.decodable.examples.cdc.txbuffering.model.TxMetaData;
import co.decodable.examples.cdc.txbuffering.model.TxMetaDataCustom;
import co.decodable.examples.cdc.txbuffering.model.TxStatus;

public class CdcTxBufferingLogic
        extends KeyedBroadcastProcessFunction<String, CdcEvent, String, TxBuffer> {

    public static final String CUSTOM_TX_META_EVENT_SCHEMA_NAME = "com.github.hpgrahsl.cdc.tx.buffering.CustomMetaDataValue";
    public static final String DEBEZIUM_TX_META_EVENT_SCHEMA_NAME = "io.debezium.connector.common.TransactionMetadataValue";
    public static final String SIDE_OUTPUT_MESSAGE_TEMPLATE = "{ \"schema\": { \"type\": \"struct\", \"fields\": [ { \"type\": \"string\", \"optional\": false, \"field\": \"status\" }, { \"type\": \"string\", \"optional\": false, \"field\": \"id\" }, { \"type\": \"int64\", \"optional\": false, \"field\": \"ts_ms\" }, { \"type\": \"string\", \"optional\": false, \"field\": \"uuid\" } ], \"optional\": false, \"name\": \""
            + CUSTOM_TX_META_EVENT_SCHEMA_NAME + "\", \"version\": 1 } }";
    public static final int INITIAL_TIMEOUT_TX_CHRONO_CHECK_TIMER_MS = 10;
    public static final int REVISIT_TIMEOUT_TX_CHRONO_CHECK_TIMER_MS = 50;

    private static transient final Logger LOGGER = LoggerFactory.getLogger(CdcTxBufferingLogic.class);
    private static transient final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static transient final MapStateDescriptor<String, TxMetaData> BROADCAST_STATE_DESCRIPTOR_LATEST_TX_META = new MapStateDescriptor<String, TxMetaData>(
            "latest-tx-meta", Types.STRING, Types.POJO(TxMetaData.class));

    public static transient final MapStateDescriptor<String, Long> BROADCAST_STATE_DESCRIPTOR_TXID_ORDER_LUT = new MapStateDescriptor<String, Long>(
            "txid-order-lut", Types.STRING, Types.LONG);

    public static transient final MapStateDescriptor<Long, String> BROADCAST_STATE_DESCRIPTOR_ORDER_TXID_LUT = new MapStateDescriptor<Long, String>(
            "order-txid-lut", Types.LONG, Types.STRING);

    public static transient final MapStateDescriptor<String, Long> BROADCAST_STATE_DESCRIPTOR_TX_CHRONO_INDEX = new MapStateDescriptor<String, Long>(
            "tx-chrono-index", Types.STRING, Types.LONG);

    ValueState<CdcTxBuffer> keyedTxBufferState;
    final OutputTag<String> txBufferingSideOutput = new OutputTag<String>("tx-buffering-side-output") {};
    final String uniqueProcessingId;

    public CdcTxBufferingLogic(String uniqueProcessingId) {
        LOGGER.debug("cdc tx buffering logic running with processing id {}",uniqueProcessingId);
        this.uniqueProcessingId = uniqueProcessingId;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        ValueStateDescriptor<CdcTxBuffer> keyedStateValueDescriptor = new ValueStateDescriptor<CdcTxBuffer>(
                "transaction-buffer",
                Types.POJO(CdcTxBuffer.class));
        keyedTxBufferState = getRuntimeContext().getState(keyedStateValueDescriptor);
    }

    @Override
    public void processElement(CdcEvent rawEvent,
            KeyedBroadcastProcessFunction<String, CdcEvent, String, TxBuffer>.ReadOnlyContext ctx, Collector<TxBuffer> out)
            throws Exception {

        LOGGER.debug("received cdc event via keyed stream: {}", rawEvent.getValue());

        String txId = ctx.getCurrentKey();
        JsonNode json = OBJECT_MAPPER.readTree(rawEvent.getValue());
        String eventSchemaName = json.get("schema").get("name").asText();

        if (DEBEZIUM_TX_META_EVENT_SCHEMA_NAME.equals(eventSchemaName)) {
            LOGGER.trace("handling tx meta event for tx id '{}'", txId);
            processKeyedTxMetaDataEvent(txId, json, rawEvent);
        } else {
            LOGGER.trace("handling cdc event for tx id '{}'", txId);
            processKeyedCdcEvent(txId, json, rawEvent);
        }

        if (keyedTxBufferState.value().getCompleted()) {
            LOGGER.info("detected complete transaction buffer for tx id '{}'",txId);     
            LOGGER.trace("register first timer for tx id '{}' to perform chronology check",txId);
            long tsTxChronoCheck = ctx.currentProcessingTime() + INITIAL_TIMEOUT_TX_CHRONO_CHECK_TIMER_MS;
            ctx.timerService().registerProcessingTimeTimer(tsTxChronoCheck);
        } else {
            LOGGER.debug("transaction buffer for tx id '{}' still incomplete",txId);
        }
    }

    @Override
    public void processBroadcastElement(String rawTxMetaEvent,
            KeyedBroadcastProcessFunction<String, CdcEvent, String, TxBuffer>.Context ctx, Collector<TxBuffer> out)
            throws Exception {
        
        LOGGER.debug("received tx meta data event via broadcast stream: {}", rawTxMetaEvent);
        JsonNode json = OBJECT_MAPPER.readTree(rawTxMetaEvent);
        var txMetaEventSchemaName = json.get("schema").get("name").asText();
        
        BroadcastState<String,TxMetaData> stateLatestTxMeta = ctx.getBroadcastState(BROADCAST_STATE_DESCRIPTOR_LATEST_TX_META);
        BroadcastState<String,Long> stateTxChronoIndex = ctx.getBroadcastState(BROADCAST_STATE_DESCRIPTOR_TX_CHRONO_INDEX);
        BroadcastState<String,Long> stateTxIdOrderLUT = ctx.getBroadcastState(BROADCAST_STATE_DESCRIPTOR_TXID_ORDER_LUT);
        BroadcastState<Long,String> stateOrderTxIdLUT  = ctx.getBroadcastState(BROADCAST_STATE_DESCRIPTOR_ORDER_TXID_LUT);
        
        if (DEBEZIUM_TX_META_EVENT_SCHEMA_NAME.equals(txMetaEventSchemaName)) {
            TxMetaData currentTxMetaData = 
                OBJECT_MAPPER.readValue(
                    OBJECT_MAPPER.writeValueAsString(json.get("payload")),TxMetaData.class);
            
            LOGGER.debug("handling tx meta data event for tx id '{}' on broadcast stream", currentTxMetaData.id());
            
            if (!stateLatestTxMeta.contains(currentTxMetaData.id())) {
                Long index = stateTxChronoIndex.get("idx");
                if (index != null) {
                    index += 1;
                    LOGGER.trace("updating tx chrono state for tx id '{}' with index {}",currentTxMetaData.id(),index);
                    stateTxChronoIndex.put("idx",index);
                } else {
                    index = 1L;
                    LOGGER.trace("init tx chrono state for tx id '{}' with index {}",currentTxMetaData.id(),index);
                    stateTxChronoIndex.put("idx",index);
                }
                if (!stateTxIdOrderLUT.contains(currentTxMetaData.id())) {
                    LOGGER.trace("updating txid-order lookup table with: <'{}' -> {}>",currentTxMetaData.id(),index);
                    stateTxIdOrderLUT.put(currentTxMetaData.id(), index);
                }
                if (!stateOrderTxIdLUT.contains(index)) {
                    LOGGER.trace("updating order-txid lookup table with: <{} -> '{}'>",index,currentTxMetaData.id());
                    stateOrderTxIdLUT.put(index, currentTxMetaData.id());
                }
            }
            stateLatestTxMeta.put(currentTxMetaData.id(),currentTxMetaData);
        } else if(CUSTOM_TX_META_EVENT_SCHEMA_NAME.equals(txMetaEventSchemaName)) {
            TxMetaDataCustom currentTxMetaDataCustom = 
            OBJECT_MAPPER.readValue(
                OBJECT_MAPPER.writeValueAsString(json.get("payload")),TxMetaDataCustom.class);
            LOGGER.debug("handling custom tx meta data event for tx id '{}' on broadcast stream", currentTxMetaDataCustom.id());
            if (currentTxMetaDataCustom.uuid().equals(uniqueProcessingId)) {
                if (currentTxMetaDataCustom.status() == TxStatus.EMITTED) {
                    LOGGER.debug("detected emitted tx id '{}' -> removing broadcast state entries",currentTxMetaDataCustom.id());
                    var txIdx = stateTxIdOrderLUT.get(currentTxMetaDataCustom.id());
                    stateOrderTxIdLUT.remove(txIdx);
                    stateTxIdOrderLUT.remove(currentTxMetaDataCustom.id());
                    stateLatestTxMeta.remove(currentTxMetaDataCustom.id());
                }
            } else {
                LOGGER.trace(
                        "UUID mismatch -> skipping over custom tx meta data event due to reprocessing or other parallel job instance");
            }
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<TxBuffer> out) throws Exception {
        
        String txId = ctx.getCurrentKey();
        LOGGER.trace("previously registered timer called for tx id '{}'",txId);
        
        ReadOnlyBroadcastState<String,Long> stateTxIdOrderLUT = ctx.getBroadcastState(BROADCAST_STATE_DESCRIPTOR_TXID_ORDER_LUT);
        ReadOnlyBroadcastState<Long,String> stateOrderTxIdLUT  = ctx.getBroadcastState(BROADCAST_STATE_DESCRIPTOR_ORDER_TXID_LUT);
        
        Long txIdx = stateTxIdOrderLUT.get(txId);
        if (txIdx == null) {
            LOGGER.trace("tx id '{}' not (yet) found in broadcast state lookup tables -> register another timer for tx id '{}' to revisit chronology check later",txId);
            long tsTxChronoCheck = ctx.currentProcessingTime() + REVISIT_TIMEOUT_TX_CHRONO_CHECK_TIMER_MS;
            ctx.timerService().registerProcessingTimeTimer(tsTxChronoCheck);
            return;
        }

        LOGGER.trace("index for tx id '{}': {}",txId,txIdx);
        String previousTxId = stateOrderTxIdLUT.get(txIdx - 1);
        LOGGER.trace("preceding tx id '{}'",previousTxId);
        if (previousTxId == null) {
            LOGGER.info("no pending transaction found preceding tx id '{}' -> emitting its buffer",txId);
            out.collect(TxBuffer.fromTransactionBuffer(keyedTxBufferState.value()));
            TxMetaDataCustom customTxMeta = new TxMetaDataCustom (txId, TxStatus.EMITTED, ctx.currentProcessingTime(),uniqueProcessingId);
            var jsonObj = (ObjectNode) OBJECT_MAPPER.readTree(SIDE_OUTPUT_MESSAGE_TEMPLATE);
            jsonObj.set("payload", OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(customTxMeta)));
            ctx.output(txBufferingSideOutput, OBJECT_MAPPER.writeValueAsString(jsonObj));
            LOGGER.debug("clearing value statue for tx buffer of tx id '{}'",txId);
            keyedTxBufferState.clear();
        } else {
            LOGGER.trace("register another timer for tx id '{}' to revisit chronology check later",txId);
            long tsTxChronoCheck = ctx.currentProcessingTime() + REVISIT_TIMEOUT_TX_CHRONO_CHECK_TIMER_MS;
            ctx.timerService().registerProcessingTimeTimer(tsTxChronoCheck);
        }
    }

    public CdcTxBuffer getOrCreateTransactionBuffer(String txId) throws IOException {
        if (keyedTxBufferState.value() != null) {
            LOGGER.trace("returning existing transaction buffer for tx id '{}'", txId);
            return keyedTxBufferState.value();
        } else {
            LOGGER.debug("creating new transaction buffer for tx id '{}'", txId);
            return new CdcTxBuffer();
        }
    }

    public void processKeyedTxMetaDataEvent(String txId, JsonNode json, CdcEvent rawEvent) throws Exception {
        CdcTxBuffer txBuffer = getOrCreateTransactionBuffer(txId);
        var txMetaData = OBJECT_MAPPER.readValue(json.get("payload").toString(), TxMetaData.class);
        if (txMetaData.status() == TxStatus.BEGIN) {
            LOGGER.debug("set BEGIN marker {} for transaction buffer for tx id '{}'", txMetaData, txId);
            txBuffer.setBeginMarker(txMetaData);
        } else if (txMetaData.status() == TxStatus.END) {
            LOGGER.debug("set END marker {} for transaction buffer for tx id '{}'", txMetaData, txId);
            txBuffer.setEndMarker(txMetaData);
        } else {
            LOGGER.trace("skipping tx meta event for tx id '{}' due to status other than {}{}", txId, TxStatus.BEGIN,
                    TxStatus.END);
        }
        LOGGER.trace("updating transaction buffer for tx id '{}' after adding meta data event", txId);
        keyedTxBufferState.update(txBuffer);
    }

    public void processKeyedCdcEvent(String txId, JsonNode json, CdcEvent rawEvent) throws Exception {
        JsonNode transactionField = json.get("payload").get("transaction");
        JsonNode sourceField = json.get("payload").get("source");

        String dataCollection = sourceField.get("db").asText()
                + "." + sourceField.get("table").asText();
        int ordinal = transactionField.get("data_collection_order").asInt();

        CdcTxBuffer txBuffer = getOrCreateTransactionBuffer(txId);
        LOGGER.debug("adding cdc event for table '{}' with ordinal '{}' to transaction buffer having tx id '{}'",
                dataCollection, ordinal, txId);
        txBuffer.putCdcEvent(dataCollection, ordinal, rawEvent);
        LOGGER.trace("updating transaction buffer for tx id '{}' after adding cdc event", txId);
        keyedTxBufferState.update(txBuffer);
    }

}
