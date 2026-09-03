package nl.kmartin.dartsmatcherapi.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Serializes MongoDB ObjectIds as their hexadecimal string representation.
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {

    /**
     * Writes the ObjectId as a JSON string.
     *
     * @param objectId           the ObjectId to serialize
     * @param jsonGenerator      the JSON generator
     * @param serializerProvider the serializer provider
     */
    @Override
    public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(objectId.toHexString());
    }
}