/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.demos.arrayagg;

import java.util.Objects;

import org.apache.flink.table.api.dataview.ListView;

public class ArrayAccumulator<T> {

	public ListView<T> values = new ListView<T>();

	@Override
	public int hashCode() {
		return Objects.hash(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayAccumulator<?> other = (ArrayAccumulator<?>) obj;
		return Objects.equals(values, other.values);
	}
}
