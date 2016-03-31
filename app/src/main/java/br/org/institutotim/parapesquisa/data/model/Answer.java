package br.org.institutotim.parapesquisa.data.model;

import android.content.ContentValues;
import android.os.Parcelable;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import auto.parcel.AutoParcel;
import br.org.institutotim.parapesquisa.util.StringUtils;

@AutoParcel
@JsonDeserialize(using = Answer.Deserializer.class)
public abstract class Answer implements Parcelable {

    public static final String TABLE = "answers";

    public static final int FORMAT_ARRAY = 0;
    public static final int FORMAT_SINGLE_VALUE = 1;

    public static final int TYPE_STRING = 0;
    public static final int TYPE_NUMBER = 1;

    public static final String FIELD_ID = "field_id";
    public static final String FORMAT = "answer_format";
    public static final String TYPE = "answer_type";
    public static final String VALUES = "answer_values";
    public static final String USER_SUBMISSION_ID = "user_submission_id";
    public static final String LAST_VALUES = "last_values";

    public static final String[] COLUMNS = {
            FIELD_ID, FORMAT, TYPE, VALUES, LAST_VALUES
    };

    public abstract long getFieldId();

    public abstract int getFormat();

    public abstract int getType();

    public abstract String getValues();

    public abstract String getLastValues();

    @JsonValue
    public Object[] getValue() {
        Object[] value = new Object[2];
        value[0] = getFieldId();

        Object[] answer = StringUtils.split(getValues(), "\\\\");

        if (getFormat() == FORMAT_ARRAY) {
            Object[] array = new Object[answer.length];
            for (int i = 0; i < answer.length; i++) {
                String string = answer[i].toString();
                array[i] = getType() == TYPE_STRING ? string : Long.parseLong(string);
            }
            value[1] = array;
        } else {
            String string = answer.length != 0 ? answer[0].toString() : "";
            if (TextUtils.isEmpty(string)) {
                return null;
            }
            value[1] = getType() == TYPE_STRING ? string : Long.parseLong(string);
        }

        return value;
    }

    @AutoParcel.Builder
    public static abstract class Builder {

        public abstract Builder fieldId(long fieldId);

        public abstract Builder format(int format);

        public abstract Builder type(int type);

        public abstract Builder values(String values);

        public abstract Builder lastValues(String values);

        public abstract Answer build();
    }

    public static class Deserializer extends StdDeserializer<Answer> {

        public Deserializer() {
            super(Answer.class);
        }

        @Override
        public Answer deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) throws IOException {
            TreeNode node = jsonparser.readValueAsTree();

            long fieldId = node.get(0).traverse().getLongValue();
            JsonParser parser = node.get(1).traverse();

            int format, type = 0;
            String value;
            JsonToken token = parser.nextToken();
            if (token.equals(JsonToken.START_ARRAY)) {
                format = FORMAT_ARRAY;
                List<String> values = new ArrayList<>();
                token = parser.nextToken();
                while (!token.equals(JsonToken.END_ARRAY)) {
                    if (token.equals(JsonToken.VALUE_STRING)) {
                        type = TYPE_STRING;
                    } else if (token.equals(JsonToken.VALUE_NUMBER_INT)) {
                        type = TYPE_NUMBER;
                    }
                    String singleValue = parser.getValueAsString();
                    if (singleValue != null && !singleValue.isEmpty()) {
                        values.add(singleValue);
                    }
                    token = parser.nextToken();
                }
                value = TextUtils.join("\\\\", values);
            } else {
                format = FORMAT_SINGLE_VALUE;
                value = parser.getValueAsString();
                if (token.equals(JsonToken.VALUE_STRING)) {
                    type = TYPE_STRING;
                } else if (token.equals(JsonToken.VALUE_NUMBER_INT)) {
                    type = TYPE_NUMBER;
                }
            }

            return new AutoParcel_Answer.Builder()
                    .fieldId(fieldId)
                    .format(format)
                    .type(type)
                    .values(value)
                    .lastValues("")
                    .build();
        }
    }

    public static class ContentBuilder {

        private final ContentValues contentValues = new ContentValues();

        public ContentBuilder fieldId(long fieldId) {
            contentValues.put(FIELD_ID, fieldId);
            return this;
        }

        public ContentBuilder format(int format) {
            contentValues.put(FORMAT, format);
            return this;
        }

        public ContentBuilder type(int type) {
            contentValues.put(TYPE, type);
            return this;
        }

        public ContentBuilder values(String values) {
            contentValues.put(VALUES, values);
            return this;
        }

        public ContentBuilder lastValues(String values) {
            contentValues.put(LAST_VALUES, values);
            return this;
        }

        public ContentBuilder userSubmissionId(long userSubmissionId) {
            contentValues.put(USER_SUBMISSION_ID, userSubmissionId);
            return this;
        }

        public ContentValues build() {
            return contentValues; // TODO defensive copy?
        }
    }

    public static Builder builder() {
        return new AutoParcel_Answer.Builder();
    }
}
