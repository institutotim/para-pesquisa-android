package br.org.institutotim.parapesquisa.data.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {

    AGENT("agent"),
    MOD("mod"),
    API("api");

    private String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public @NonNull String toString() {
        return value;
    }

    public static UserRole get(String value) {
        for (UserRole role : values()) {
            if (role.value.equals(value)) return role;
        }
        return null;
    }
}
