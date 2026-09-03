package nl.kmartin.dartsmatcherapi.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.kmartin.dartsmatcherapi.serializer.ObjectIdDeserializer;
import nl.kmartin.dartsmatcherapi.serializer.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures JSON serialization and deserialization for the application.
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates the Jackson module used to serialize and deserialize MongoDB object IDs.
     *
     * @return the ObjectId serialization module
     */
    @Bean
    public Module objectIdModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        return module;
    }

    /**
     * Configures Java time values to be serialized as numeric timestamps.
     *
     * @return the Jackson configuration customizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}