package co.decodable.examples.cdc.txbuffering.typeinfo;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;

import co.decodable.examples.cdc.txbuffering.model.TxDataCollection;

public class TxDataCollectionTypeInfoFactory extends TypeInfoFactory<List<TxDataCollection>> {

    @Override
    public TypeInformation<List<TxDataCollection>> createTypeInfo(Type t,
            Map<String, TypeInformation<?>> genericParameters) {
        return Types.LIST(Types.POJO(TxDataCollection.class));
    }

}
