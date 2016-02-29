package br.org.institutotim.parapesquisa;

import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import br.org.institutotim.parapesquisa.data.DataModule;
import br.org.institutotim.parapesquisa.data.api.response.SingleResponse;
import br.org.institutotim.parapesquisa.data.model.Answer;
import br.org.institutotim.parapesquisa.data.model.UserData;
import br.org.institutotim.parapesquisa.util.StringUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DeserializationTest {

    String rawValue = "{\"response\":{\"id\":45,\"name\":\"Mayara da Silva Gonçalves\",\"username\":\"MGonçalves\",\"role\":\"mod\",\"email\":null,\"active\":true,\"created_at\":\"2015-08-29T16:58:42.860-03:00\",\"avatar\":null}}";

    @Test
    public void shouldNotThrowException() throws IOException {
        SingleResponse<UserData> response = new DataModule().provideObjectMapper()
                .readValue(rawValue, new TypeReference<SingleResponse<UserData>>() {
                });

        assertEquals("MGonçalves", response.getResponse().getUsername());
        assertEquals("Mayara da Silva Gonçalves", response.getResponse().getName());
        assertEquals(45, response.getResponse().getId());
    }

    @Test
    public void shouldDeserializeAnswerWithArray() throws IOException {
        String json = "[1, [\"a\", \"b\"]]";

        Answer answer = new DataModule().provideObjectMapper().readValue(json, Answer.class);
        assertEquals(1, answer.getFieldId());
        assertEquals(Answer.FORMAT_ARRAY, answer.getFormat());
        assertEquals(Answer.TYPE_STRING, answer.getType());
        assertEquals("a\\\\b", answer.getValues());
    }

    @Test
    public void shouldDeserializeAnswerWithArrayNumber() throws IOException {
        String json = "[1, [2, 3]]";

        Answer answer = new DataModule().provideObjectMapper().readValue(json, Answer.class);
        assertEquals(1, answer.getFieldId());
        assertEquals(Answer.FORMAT_ARRAY, answer.getFormat());
        assertEquals(Answer.TYPE_NUMBER, answer.getType());
        assertEquals("2\\3", answer.getValues());
    }

    @Test
    public void shouldDeserializeAnswerSingleValue() throws IOException {
        String json = "[1, \"a\"]";

        Answer answer = new DataModule().provideObjectMapper().readValue(json, Answer.class);
        assertEquals(1, answer.getFieldId());
        assertEquals(Answer.FORMAT_SINGLE_VALUE, answer.getFormat());
        assertEquals(Answer.TYPE_STRING, answer.getType());
        assertEquals("a", answer.getValues());
    }

    @Test
    public void shouldDeserializeAnswerSingleValueNumber() throws IOException {
        String json = "[1, 2]";

        Answer answer = new DataModule().provideObjectMapper().readValue(json, Answer.class);
        assertEquals(1, answer.getFieldId());
        assertEquals(Answer.FORMAT_SINGLE_VALUE, answer.getFormat());
        assertEquals(Answer.TYPE_NUMBER, answer.getType());
        assertEquals("2", answer.getValues());
    }

    @Test
    public void weirdAnswer() throws IOException {
        String json = "[66,[\"Divorciado, desquitado ou separado judicialmente\"]]";

        Answer answer = new DataModule().provideObjectMapper().readValue(json, Answer.class);
        assertEquals(66, answer.getFieldId());
        assertEquals("Divorciado, desquitado ou separado judicialmente", answer.getValues());
        assertEquals(Answer.FORMAT_ARRAY, answer.getFormat());
        assertEquals(Answer.TYPE_STRING, answer.getType());
        assertEquals(1, StringUtils.split(answer.getValues(), "\\\\").length);

        String result = new DataModule().provideObjectMapper().writeValueAsString(answer);

        assertEquals(json, result);
    }

    @Test
    public void weirdAnswer2() throws IOException {
        String json = "[66,[\"Spotify\",\"Facebook\"]]";

        Answer answer = new DataModule().provideObjectMapper().readValue(json, Answer.class);
        assertEquals(66, answer.getFieldId());
        assertEquals("Spotify\\\\Facebook", answer.getValues());
        assertEquals(Answer.FORMAT_ARRAY, answer.getFormat());
        assertEquals(Answer.TYPE_STRING, answer.getType());
        assertEquals(2, StringUtils.split(answer.getValues(), "\\\\").length);

        String result = new DataModule().provideObjectMapper().writeValueAsString(answer);

        assertEquals(json, result);
    }
}
