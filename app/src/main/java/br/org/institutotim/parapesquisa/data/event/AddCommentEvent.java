package br.org.institutotim.parapesquisa.data.event;

import br.org.institutotim.parapesquisa.data.model.Field;

public class AddCommentEvent {

    private final Field field;
    private final String comment;

    public AddCommentEvent(Field field, String comment) {
        this.field = field;
        this.comment = comment;
    }

    public Field getField() {
        return field;
    }

    public String getComment() {
        return comment;
    }
}
