package de.tum.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.io.IOException;

@Component
public class CompletedPartDeserializer extends StdDeserializer<CompletedPart> {

    public CompletedPartDeserializer() {
        this(null);
    }

    public CompletedPartDeserializer(Class<CompletedPart> t) {
        super(t);
    }

    @Override
    public CompletedPart deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        return CompletedPart.builder()
                .partNumber(jsonNode.get("partNumber").intValue())
                .eTag(jsonNode.get("eTag").asText())
                .build();
    }
}