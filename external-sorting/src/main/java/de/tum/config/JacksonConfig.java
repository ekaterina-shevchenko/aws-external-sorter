package de.tum.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.tum.data.CompletedPartDeserializer;
import de.tum.data.CompletedPartSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import software.amazon.awssdk.services.s3.model.CompletedPart;

@Configuration
public class JacksonConfig {
    @Autowired
    private CompletedPartDeserializer completedPartDeserializer;
    @Autowired
    private CompletedPartSerializer completedPartSerializer;

    @Bean
    public ObjectMapper jsonObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .serializerByType(CompletedPart.class, completedPartSerializer)
                .deserializerByType(CompletedPart.class, completedPartDeserializer)
                .build();
    }
}