package com.msrecette.univ.soa.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.msrecette.univ.soa.validation.ValidIngredient;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ValidIngredient(message = "L'ID ou le nom de l'aliment est requis pour chaque ingrédient")
@Schema(description = "Ingrédient d'une recette")
public class IngredientRequest {

    @Schema(description = "ID de l'aliment (optionnel si alimentNom est fourni)", example = "5")
    private Long alimentId;

    @Schema(description = "Nom de l'aliment - peut être le nom d'un aliment existant ou un nom libre", example = "Spaghetti")
    private String alimentNom;

    @Schema(description = "Quantité", example = "200.0")
    @DecimalMin(value = "0.01", message = "La quantité doit être supérieure à 0")
    private Float quantite;

    @Schema(description = "Unité de mesure", example = "GRAMME", allowableValues = {"GRAMME", "KILOGRAMME", "LITRE", "MILLILITRE", "CUILLERE_A_SOUPE", "CUILLERE_A_CAFE", "SACHET", "UNITE"})
    private String unite;

    @Schema(description = "Indique si c'est un ingrédient principal", example = "true")
    private Boolean principal;
}
