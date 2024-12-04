package co.decodable.examples.cdc.txbuffering.typeinfo;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import co.decodable.examples.cdc.txbuffering.model.CdcEvent;

public class TxBufferTypeInfoFactory extends TypeInfoFactory<Map<String,Map<Integer,CdcEvent>>> {

    @Override
    public TypeInformation<Map<String, Map<Integer, CdcEvent>>> createTypeInfo(Type t,
            Map<String, TypeInformation<?>> genericParameters) {
        return Types.MAP(Types.STRING, Types.MAP(Types.INT, Types.POJO(CdcEvent.class)));
    }

}
