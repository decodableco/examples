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

	@Override
	public ArrayAccumulator<T> createAccumulator() {
		return new ArrayAccumulator<T>();
	}

	@Override
	public T[] getValue(ArrayAccumulator<T> acc) {
		if (acc.added.getList().isEmpty()) {
			return null;
		} else {
			Class<?> elementType = getElementType(acc);
			if (elementType != null) {
				List<T> added = new ArrayList<T>(acc.added.getList());
				added.removeAll(acc.retracted.getList());

				return added.toArray((T[]) Array.newInstance(elementType, added.size()));
			}

			return null;
		}
	}

	private Class<?> getElementType(ArrayAccumulator<T> acc) {
		try {
			for (Object row : acc.added.get()) {
				if (row != null) {
					return row.getClass();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void accumulate(ArrayAccumulator<T> acc, T o) throws Exception {
		System.out.println("Adding: " + o);

		if (o != null) {
			acc.added.add(o);
		}
	}

	public void retract(ArrayAccumulator<T> acc, T o) throws Exception {
		System.out.println("Retracting: " + o);

		if (o != null) {
			acc.retracted.add(o);
		}
	}

	public void merge(ArrayAccumulator<T> acc, Iterable<ArrayAccumulator<T>> it) throws Exception {
		for (ArrayAccumulator<T> other : it) {
			acc.added.addAll(other.added.getList());
			acc.retracted.addAll(other.retracted.getList());
		}
	}

	public void resetAccumulator(ArrayAccumulator<T> acc) {
		acc.added.clear();
		acc.retracted.clear();
	}

	@Override
	public TypeInference getTypeInference(DataTypeFactory typeFactory) {
		return TypeInference.newBuilder()
				.inputTypeStrategy(InputTypeStrategies.sequence(InputTypeStrategies.ANY))
				.accumulatorTypeStrategy(ctx -> {
					return Optional.of(
							DataTypes.STRUCTURED(
									ArrayAccumulator.class,
									DataTypes.FIELD("added",ListView.newListViewDataType(ctx.getArgumentDataTypes().get(0))),
									DataTypes.FIELD("retracted",ListView.newListViewDataType(ctx.getArgumentDataTypes().get(0)))
							));
				})
				.outputTypeStrategy(ctx -> {
					DataType argDataType = ctx.getArgumentDataTypes().get(0);
					return Optional.of(DataTypes.ARRAY(argDataType));
				}).build();
	}
}