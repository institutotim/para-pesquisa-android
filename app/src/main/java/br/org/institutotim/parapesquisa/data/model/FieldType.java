package br.org.institutotim.parapesquisa.data.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {

    TEXT("TextField"),
    CPF("CpfField"),
    LABEL("LabelField"),
    EMAIL("EmailField"),
    MONEY("DinheiroField"),
    DATETIME("DatetimeField"),
    CHECKBOX("CheckboxField"),
    PRIVATE("PrivateField"),
    RADIO("RadioField"),
    SELECT("SelectField"),
    URL("UrlField"),
    ORDERED_LIST("OrderedlistField"),
    NUMBER("NumberField");

    private String value;

    FieldType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    public static FieldType get(String value) {
        for (FieldType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
