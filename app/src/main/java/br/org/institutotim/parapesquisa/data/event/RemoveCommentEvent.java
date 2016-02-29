package br.org.institutotim.parapesquisa.data.event;

import br.org.institutotim.parapesquisa.data.model.Field;

public class RemoveCommentEvent {

    private final Field field;

    public RemoveCommentEvent(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}
