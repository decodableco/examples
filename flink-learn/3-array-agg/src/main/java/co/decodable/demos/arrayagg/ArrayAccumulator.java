/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright Decodable, Inc.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package co.decodable.demos.arrayagg;

import org.apache.flink.table.api.dataview.ListView;

public class ArrayAccumulator<T> {
	public ListView<T> added = new ListView<T>();
	public ListView<T> retracted = new ListView<T>();
}
