package br.org.institutotim.parapesquisa.data.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldLayout {

    SMALL("small"),
    MEDIUM("medium"),
    BIG("big"),
    SINGLE_COLUMN("single_column"),
    MULTIPLE_COLUMNS("multiple_columns");

    private String value;

    FieldLayout(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    public static FieldLayout get(String value) {
        for (FieldLayout layout : values()) {
            if (layout.value.equals(value)) {
                return layout;
            }
        }

        return null;
    }
}
