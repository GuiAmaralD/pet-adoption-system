package com.example.auth.pet.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;


@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Specie {
    DOG, CAT, BIRD;

    @JsonCreator
    public static Specie fromString(String value) {
        return value == null ? null : Specie.valueOf(value.toUpperCase());
    }
}
