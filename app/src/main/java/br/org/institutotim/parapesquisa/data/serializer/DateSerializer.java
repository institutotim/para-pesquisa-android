package br.org.institutotim.parapesquisa.data.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.joda.time.DateTime;

import java.io.IOException;

public class DateSerializer extends StdSerializer<DateTime> {

    public DateSerializer() {
        super(DateTime.class);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(new DateTime(value).toString());
    }
}
