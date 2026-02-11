package com.example.auth.Pet.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pet Enums Tests")
class PetEnumsTest {

    // ==================== SPECIE TESTS ====================

    @Test
    @DisplayName("Specie.fromString should convert valid values in lowercase")
    void specieFromString_shouldConvertValidValuesInLowercase() {
        assertEquals(Specie.DOG, Specie.fromString("dog"));
        assertEquals(Specie.CAT, Specie.fromString("cat"));
        assertEquals(Specie.BIRD, Specie.fromString("bird"));
    }

    @Test
    @DisplayName("Specie.fromString should convert valid values in uppercase")
    void specieFromString_shouldConvertValidValuesInUppercase() {
        assertEquals(Specie.DOG, Specie.fromString("DOG"));
        assertEquals(Specie.CAT, Specie.fromString("CAT"));
        assertEquals(Specie.BIRD, Specie.fromString("BIRD"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DoG", "CaT", "bIrD"})
    @DisplayName("Specie.fromString should convert values with mixed case")
    void specieFromString_shouldConvertMixedCaseValues(String value) {
        assertNotNull(Specie.fromString(value));
    }

    @Test
    @DisplayName("Specie.fromString should return null when value is null")
    void specieFromString_shouldReturnNullWhenValueIsNull() {
        assertNull(Specie.fromString(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"HAMSTER", "FISH", "RABBIT", "invalid", ""})
    @DisplayName("Specie.fromString should throw exception for invalid values")
    void specieFromString_shouldThrowExceptionForInvalidValues(String invalidValue) {
        assertThrows(IllegalArgumentException.class,
                () -> Specie.fromString(invalidValue));
    }

    // ==================== SEX TESTS ====================

    @Test
    @DisplayName("Sex.fromString should convert valid values in lowercase")
    void sexFromString_shouldConvertValidValuesInLowercase() {
        assertEquals(Sex.MALE, Sex.fromString("male"));
        assertEquals(Sex.FEMALE, Sex.fromString("female"));
    }

    @Test
    @DisplayName("Sex.fromString should convert valid values in uppercase")
    void sexFromString_shouldConvertValidValuesInUppercase() {
        assertEquals(Sex.MALE, Sex.fromString("MALE"));
        assertEquals(Sex.FEMALE, Sex.fromString("FEMALE"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MaLe", "fEmAlE"})
    @DisplayName("Sex.fromString should convert values with mixed case")
    void sexFromString_shouldConvertMixedCaseValues(String value) {
        assertNotNull(Sex.fromString(value));
    }

    @Test
    @DisplayName("Sex.fromString should return null when value is null")
    void sexFromString_shouldReturnNullWhenValueIsNull() {
        assertNull(Sex.fromString(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN", "OTHER", "invalid", ""})
    @DisplayName("Sex.fromString should throw exception for invalid values")
    void sexFromString_shouldThrowExceptionForInvalidValues(String invalidValue) {
        assertThrows(IllegalArgumentException.class,
                () -> Sex.fromString(invalidValue));
    }

    // ==================== SIZE TESTS ====================

    @Test
    @DisplayName("Size.fromString should convert valid values in lowercase")
    void sizeFromString_shouldConvertValidValuesInLowercase() {
        assertEquals(Size.SMALL, Size.fromString("small"));
        assertEquals(Size.MEDIUM, Size.fromString("medium"));
        assertEquals(Size.BIG, Size.fromString("big"));
    }

    @Test
    @DisplayName("Size.fromString should convert valid values in uppercase")
    void sizeFromString_shouldConvertValidValuesInUppercase() {
        assertEquals(Size.SMALL, Size.fromString("SMALL"));
        assertEquals(Size.MEDIUM, Size.fromString("MEDIUM"));
        assertEquals(Size.BIG, Size.fromString("BIG"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SmAlL", "mEdIuM", "BiG"})
    @DisplayName("Size.fromString should convert values with mixed case")
    void sizeFromString_shouldConvertMixedCaseValues(String value) {
        assertNotNull(Size.fromString(value));
    }

    @Test
    @DisplayName("Size.fromString should return null when value is null")
    void sizeFromString_shouldReturnNullWhenValueIsNull() {
        assertNull(Size.fromString(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"LARGE", "TINY", "HUGE", "invalid", ""})
    @DisplayName("Size.fromString should throw exception for invalid values")
    void sizeFromString_shouldThrowExceptionForInvalidValues(String invalidValue) {
        assertThrows(IllegalArgumentException.class,
                () -> Size.fromString(invalidValue));
    }

    // ==================== ADDITIONAL TESTS ====================

    @Test
    @DisplayName("All enums should have defined values")
    void allEnums_shouldHaveDefinedValues() {
        assertEquals(3, Specie.values().length);
        assertEquals(2, Sex.values().length);
        assertEquals(3, Size.values().length);
    }

    @Test
    @DisplayName("Specie enum should contain expected constants")
    void specieEnum_shouldContainExpectedConstants() {
        assertNotNull(Specie.valueOf("DOG"));
        assertNotNull(Specie.valueOf("CAT"));
        assertNotNull(Specie.valueOf("BIRD"));
    }

    @Test
    @DisplayName("Sex enum should contain expected constants")
    void sexEnum_shouldContainExpectedConstants() {
        assertNotNull(Sex.valueOf("MALE"));
        assertNotNull(Sex.valueOf("FEMALE"));
    }

    @Test
    @DisplayName("Size enum should contain expected constants")
    void sizeEnum_shouldContainExpectedConstants() {
        assertNotNull(Size.valueOf("SMALL"));
        assertNotNull(Size.valueOf("MEDIUM"));
        assertNotNull(Size.valueOf("BIG"));
    }
}