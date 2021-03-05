package de.tum.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.io.IOException;

@Component
public class CompletedPartSerializer extends StdSerializer<CompletedPart> {

    public CompletedPartSerializer() {
        this(null);
    }

    public CompletedPartSerializer(Class<CompletedPart> t) {
        super(t);
    }

    @Override
    public void serialize(CompletedPart value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("partNumber", value.partNumber());
        jgen.writeStringField("eTag", value.eTag());
        jgen.writeEndObject();
    }
}