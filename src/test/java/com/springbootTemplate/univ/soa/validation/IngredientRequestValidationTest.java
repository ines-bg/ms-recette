package com.springbootTemplate.univ.soa.validation;

import com.springbootTemplate.univ.soa.request.IngredientRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de validation pour IngredientRequest")
class IngredientRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Devrait accepter un ingrédient avec alimentId")
    void shouldAcceptIngredientWithAlimentId() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentId(1L)
                .quantite(200.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertTrue(violations.isEmpty(), "L'ingrédient avec alimentId devrait être valide");
    }

    @Test
    @DisplayName("Devrait accepter un ingrédient avec alimentNom")
    void shouldAcceptIngredientWithAlimentNom() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentNom("Pâtes fraîches")
                .quantite(200.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertTrue(violations.isEmpty(), "L'ingrédient avec alimentNom devrait être valide");
    }

    @Test
    @DisplayName("Devrait accepter un ingrédient avec les deux (alimentId et alimentNom)")
    void shouldAcceptIngredientWithBoth() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentId(1L)
                .alimentNom("Pâtes")
                .quantite(200.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertTrue(violations.isEmpty(), "L'ingrédient avec les deux champs devrait être valide");
    }

    @Test
    @DisplayName("Devrait rejeter un ingrédient sans alimentId ni alimentNom")
    void shouldRejectIngredientWithoutAlimentIdAndNom() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .quantite(200.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertFalse(violations.isEmpty(), "L'ingrédient sans alimentId ni alimentNom devrait être invalide");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("L'ID ou le nom de l'aliment est requis"));
    }

    @Test
    @DisplayName("Devrait rejeter un ingrédient avec alimentNom vide")
    void shouldRejectIngredientWithEmptyAlimentNom() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentNom("   ")
                .quantite(200.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertFalse(violations.isEmpty(), "L'ingrédient avec alimentNom vide devrait être invalide");
    }

    @Test
    @DisplayName("Devrait rejeter une quantité négative")
    void shouldRejectNegativeQuantity() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentId(1L)
                .quantite(-10.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertFalse(violations.isEmpty(), "La quantité négative devrait être rejetée");
        assertTrue(violations.stream().anyMatch(v ->
            v.getMessage().contains("quantité doit être supérieure à 0")
        ));
    }

    @Test
    @DisplayName("Devrait rejeter une quantité à zéro")
    void shouldRejectZeroQuantity() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentId(1L)
                .quantite(0.0f)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertFalse(violations.isEmpty(), "La quantité à zéro devrait être rejetée");
    }

    @Test
    @DisplayName("Devrait accepter un ingrédient sans quantité (optionnel)")
    void shouldAcceptIngredientWithoutQuantity() {
        // Given
        IngredientRequest ingredient = IngredientRequest.builder()
                .alimentId(1L)
                .unite("GRAMME")
                .principal(true)
                .build();

        // When
        Set<ConstraintViolation<IngredientRequest>> violations = validator.validate(ingredient);

        // Then
        assertTrue(violations.isEmpty(), "L'ingrédient sans quantité devrait être valide (optionnel)");
    }
}

