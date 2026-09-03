package nl.kmartin.dartsmatcherapi.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.types.ObjectId;

import java.io.IOException;

/**
 * Deserializes hexadecimal ObjectId strings into MongoDB ObjectIds.
 */
public class ObjectIdDeserializer extends JsonDeserializer<ObjectId> {

    /**
     * Creates an ObjectId from the JSON string value.
     *
     * @param jsonParser the JSON parser
     * @param context    the deserialization context
     * @return the deserialized ObjectId
     */
    @Override
    public ObjectId deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        return new ObjectId(jsonParser.getText());
    }
}