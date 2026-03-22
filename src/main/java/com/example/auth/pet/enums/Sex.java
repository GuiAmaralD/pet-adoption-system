package com.example.auth.pet.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Sex {
    MALE, FEMALE;

    @JsonCreator
    public static Sex fromString(String value) {
        return value == null ? null : Sex.valueOf(value.toUpperCase());
    }
}
