package com.example.auth.pet.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Size {
    SMALL, MEDIUM, BIG;

    @JsonCreator
    public static Size fromString(String value) {
        return value == null ? null : Size.valueOf(value.toUpperCase());
    }
}