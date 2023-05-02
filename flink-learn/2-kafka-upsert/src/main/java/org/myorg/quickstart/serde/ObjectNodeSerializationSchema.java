package org.myorg.quickstart.serde;

import org.apache.flink.api.common.serialization.SerializationSchema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectNodeSerializationSchema implements SerializationSchema<ObjectNode> {

	private final ObjectMapper mapper;
	private final boolean isKey;

	public ObjectNodeSerializationSchema(boolean isKey) {
		mapper = new ObjectMapper();
		this.isKey = isKey;
	}

	@Override
	public byte[] serialize(ObjectNode element) {
		try {
			return mapper.writeValueAsBytes(element.get(isKey ? "key" : "value"));
		}
		catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
