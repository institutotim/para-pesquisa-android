package br.org.institutotim.parapesquisa.data.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.joda.time.DateTime;

import java.io.IOException;

public class DateDeserializer extends StdDeserializer<DateTime> {

    public DateDeserializer() {
        super(DateTime.class);
    }

    @Override
    public DateTime deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) throws IOException {
        return new DateTime(jsonparser.getText());
    }
}
