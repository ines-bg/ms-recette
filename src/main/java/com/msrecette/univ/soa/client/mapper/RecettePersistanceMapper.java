package com.msrecette.univ.soa.client.mapper;

import com.msrecette.univ.soa.response.RecetteResponse;
import com.msrecette.univ.soa.response.IngredientResponse;
import com.msrecette.univ.soa.response.EtapeResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir les DTOs de ms-persistance vers les responses de ms-recette
 * Optimisé pour gérer les deux formats: Light (sans collections) et Full (avec collections)
 */
@Component
public class RecettePersistanceMapper {

    /**
     * Convertir un DTO persistance (light ou full) en response RecetteResponse
     */
    public RecetteResponse toResponse(Object persistanceDto) {
        if (persistanceDto == null) {
            return null;
        }

        // Utiliser la réflexion pour récupérer les champs (compatible light et full)
        try {
            Long id = getFieldValue(persistanceDto, "id");
            String titre = getFieldValue(persistanceDto, "titre");
            String description = getFieldValue(persistanceDto, "description");
            Integer tempsTotal = getFieldValue(persistanceDto, "tempsTotal");
            Integer kcal = getFieldValue(persistanceDto, "kcal");
            String imageUrl = getFieldValue(persistanceDto, "imageUrl");
            String difficulte = getFieldValue(persistanceDto, "difficulte");
            String categorie = getFieldValue(persistanceDto, "categorie");
            LocalDateTime dateCreation = getFieldValue(persistanceDto, "dateCreation");
            LocalDateTime dateModification = getFieldValue(persistanceDto, "dateModification");
            Boolean actif = getFieldValue(persistanceDto, "actif");
            String statut = getFieldValue(persistanceDto, "statut");
            String motifRejet = getFieldValue(persistanceDto, "motifRejet");
            Long utilisateurId = getFieldValue(persistanceDto, "utilisateurId");
            Double noteMoyenne = getFieldValue(persistanceDto, "moyenneEvaluation");
            if (noteMoyenne == null) {
                noteMoyenne = getFieldValue(persistanceDto, "noteMoyenne");
            }

            // Collections (optionnelles)
            List<?> ingredientsRaw = getFieldValue(persistanceDto, "ingredients");
            List<?> etapesRaw = getFieldValue(persistanceDto, "etapes");

            List<IngredientResponse> ingredients = mapIngredients(ingredientsRaw);
            List<EtapeResponse> etapes = mapEtapes(etapesRaw);

            // Construction via builder pour éviter les problèmes d'ordre d'arguments
            return RecetteResponse.builder()
                    .id(id)
                    .titre(titre)
                    .description(description)
                    .tempsTotal(tempsTotal)
                    .kcal(kcal)
                    .imageUrl(imageUrl)
                    .difficulte(difficulte)
                    .categorie(categorie)
                    .dateCreation(dateCreation)
                    .dateModification(dateModification)
                    .actif(actif)
                    .statut(statut)
                    .motifRejet(motifRejet)
                    .utilisateurId(utilisateurId)
                    .ingredients(ingredients)
                    .etapes(etapes)
                    .noteMoyenne(noteMoyenne)
                    // tags et nombreFeedbacks non fournis par ms-persistance → laissés à null
                    .build();

        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur lors du mapping du DTO persistance vers RecetteResponse", e);
        }
    }

    /**
     * Récupérer un champ d'un objet via réflexion
     */
    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object obj, String fieldName) {
        try {
            // Records utilisant des accesseurs directs
            java.lang.reflect.Method method = obj.getClass().getMethod(fieldName);
            Object value = method.invoke(obj);
            return (T) value;
        } catch (NoSuchMethodException e) {
            // Fallback sur les getters classiques
            try {
                java.lang.reflect.Method method = obj.getClass()
                        .getMethod("get" + capitalize(fieldName));
                Object value = method.invoke(obj);
                return (T) value;
            } catch (Exception ex) {
                return null; // Champ optionnel
            }
        } catch (Exception e) {
            return null; // Champ optionnel
        }
    }

    /**
     * Mapper les ingrédients
     */
    private List<IngredientResponse> mapIngredients(List<?> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return null;
        }

        return ingredients.stream()
                .map(ing -> {
                    Long id = getFieldValue(ing, "id");
                    Long alimentId = getFieldValue(ing, "alimentId");
                    String alimentNom = getFieldValue(ing, "alimentNom");
                    Float quantite = getFieldValue(ing, "quantite");
                    String unite = getFieldValue(ing, "unite");
                    Boolean principal = getFieldValue(ing, "principal");

                    return IngredientResponse.builder()
                            .id(id)
                            .alimentId(alimentId)
                            .alimentNom(alimentNom)
                            .quantite(quantite)
                            .unite(unite)
                            .principal(principal)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Mapper les étapes
     */
    private List<EtapeResponse> mapEtapes(List<?> etapes) {
        if (etapes == null || etapes.isEmpty()) {
            return null;
        }

        return etapes.stream()
                .map(etape -> {
                    Long id = getFieldValue(etape, "id");
                    Integer ordre = getFieldValue(etape, "ordre");
                    Integer temps = getFieldValue(etape, "temps");
                    String texte = getFieldValue(etape, "texte");

                    return EtapeResponse.builder()
                            .id(id)
                            .ordre(ordre)
                            .temps(temps)
                            .texte(texte)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Capitaliser un string pour les getters
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}

