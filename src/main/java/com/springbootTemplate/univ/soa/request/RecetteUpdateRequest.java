package com.springbootTemplate.univ.soa.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.springbootTemplate.univ.soa.response.DifficulteEnum;
import com.springbootTemplate.univ.soa.response.StatutRecetteEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Requête pour mettre à jour une recette")
public class RecetteUpdateRequest {

    @Schema(description = "Nouveau titre de la recette", example = "Pâtes Carbonara Revisitées")
    @Size(min = 3, max = 200, message = "Le titre doit contenir entre 3 et 200 caractères")
    private String titre;

    @Schema(description = "Nouvelle description")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @Schema(description = "Nouveau temps total en minutes", example = "25")
    @Min(value = 1, message = "Le temps total doit être supérieur à 0")
    @Max(value = 1440, message = "Le temps total ne peut pas dépasser 1440 minutes")
    private Integer tempsTotal;

    @Schema(description = "Nouvelles calories", example = "450")
    @Min(value = 0, message = "Les calories ne peuvent pas être négatives")
    @Max(value = 10000, message = "Les calories semblent excessives")
    private Integer kcal;

    @Schema(description = "Nouvelle URL d'image")
    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String imageUrl;

    @Schema(description = "Nouveau niveau de difficulté")
    private DifficulteEnum difficulte;

    @Schema(description = "Nouvelle catégorie")
    private String categorie;

    @Schema(description = "Nouveaux ingrédients (remplace la liste existante)")
    private List<IngredientRequest> ingredients;

    @Schema(description = "Nouvelles étapes (remplace la liste existante)")
    private List<EtapeRequest> etapes;

    @Schema(description = "Nouveaux tags")
    private List<String> tags;

    @Schema(description = "Indique si la recette est active")
    private Boolean actif;

    @Schema(description = "Nouveau statut de la recette")
    private StatutRecetteEnum statut;

    @Schema(description = "Motif de rejet si la recette est refusée")
    @Size(max = 500, message = "Le motif de rejet ne peut pas dépasser 500 caractères")
    private String motifRejet;
}
