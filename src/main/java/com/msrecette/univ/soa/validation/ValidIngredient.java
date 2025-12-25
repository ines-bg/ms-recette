package com.msrecette.univ.soa.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation pour valider qu'un ingrédient a soit un alimentId soit un alimentNom
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidIngredientValidator.class)
@Documented
public @interface ValidIngredient {
    String message() default "Un ingrédient doit avoir soit un alimentId soit un alimentNom";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

