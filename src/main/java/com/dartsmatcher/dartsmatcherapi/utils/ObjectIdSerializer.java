package com.dartsmatcher.dartsmatcherapi.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Used in Jackson to serialize ObjectId to it's id.
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {
	@Override
	public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(objectId.toHexString());
	}
}
