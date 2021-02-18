package com.dartsmatcher.dartsmatcherapi.config;

import com.dartsmatcher.dartsmatcherapi.utils.ObjectIdSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.ArrayList;

@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper jsonObjectMapper() {
		ArrayList<Module> modules = new ArrayList<>();

		//ObjectId Serialization
		SimpleModule objectIdSerializerModule = new SimpleModule();
		objectIdSerializerModule.addSerializer(ObjectId.class, new ObjectIdSerializer());

		modules.add(objectIdSerializerModule);

		return Jackson2ObjectMapperBuilder.json()
				.modules(modules)
				.build();
	}
}

