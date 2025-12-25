package com.msrecette.univ.soa.validation;

import com.msrecette.univ.soa.request.IngredientRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validateur pour s'assurer qu'un ingrédient a soit un alimentId soit un alimentNom
 */
public class ValidIngredientValidator implements ConstraintValidator<ValidIngredient, IngredientRequest> {

    @Override
    public void initialize(ValidIngredient constraintAnnotation) {
    }

    @Override
    public boolean isValid(IngredientRequest ingredient, ConstraintValidatorContext context) {
        if (ingredient == null) {
            return true; // null values are handled by @NotNull
        }

        // Au moins un des deux doit être présent
        boolean hasAlimentId = ingredient.getAlimentId() != null;
        boolean hasAlimentNom = ingredient.getAlimentNom() != null && !ingredient.getAlimentNom().trim().isEmpty();

        return hasAlimentId || hasAlimentNom;
    }
}

