/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.demos.arrayagg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.dataview.ListView;
import org.apache.flink.table.catalog.DataTypeFactory;
import org.apache.flink.table.functions.AggregateFunction;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.inference.InputTypeStrategies;
import org.apache.flink.table.types.inference.TypeInference;

public class ArrayAggr <T> extends AggregateFunction<T[], ArrayAccumulator<T>> {

	private static final long serialVersionUID = 6560271654419701770L;
	private DataType elementType;

	@Override
	public ArrayAccumulator<T> createAccumulator() {
		return new ArrayAccumulator<T>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] getValue(ArrayAccumulator<T> acc) {
		if (acc.values.getList().isEmpty()) {
			return null;
		}
		else {
			List<T> values = new ArrayList<T>(acc.values.getList());
			return values.toArray((T[]) Array.newInstance(elementType.getConversionClass(), values.size()));
		}
	}

	public void accumulate(ArrayAccumulator<T> acc, T o) throws Exception {
		if (o != null) {
			acc.values.add(o);
		}
	}

	public void retract(ArrayAccumulator<T> acc, T o) throws Exception {
		if (o != null) {
			acc.values.remove(o);
		}
	}

	public void resetAccumulator(ArrayAccumulator<T> acc) {
		acc.values.clear();
	}

	@Override
	public TypeInference getTypeInference(DataTypeFactory typeFactory) {
		return TypeInference.newBuilder()
				.inputTypeStrategy(InputTypeStrategies.sequence(InputTypeStrategies.ANY))
				.accumulatorTypeStrategy(ctx -> {
					return Optional.of(
							DataTypes.STRUCTURED(
									ArrayAccumulator.class,
									DataTypes.FIELD("values",ListView.newListViewDataType(ctx.getArgumentDataTypes().get(0)))//,
							));
				})
				.outputTypeStrategy(ctx -> {
					this.elementType = ctx.getArgumentDataTypes().get(0);
					return Optional.of(DataTypes.ARRAY(elementType));
				}).build();
	}
}
