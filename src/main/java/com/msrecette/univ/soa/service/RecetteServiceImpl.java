package com.msrecette.univ.soa.service;

import com.msrecette.univ.soa.client.RecetteClient;
import com.msrecette.univ.soa.exception.RecetteNotFoundException;
import com.msrecette.univ.soa.request.*;
import com.msrecette.univ.soa.response.RecetteResponse;
import com.msrecette.univ.soa.response.RecetteStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecetteServiceImpl implements RecetteService {

    private final RecetteClient recetteClient;

    @Override
    @CacheEvict(value = {"recettes", "recettesByCategorie"}, allEntries = true)
    public RecetteResponse createRecette(RecetteCreateRequest request) {
        log.info("Création d'une nouvelle recette: {}", request.getTitre());
        log.info("🔍 utilisateurId reçu dans ms-recette: {}", request.getUtilisateurId());

        // Valider d'abord avant toute communication avec le client
        validateRecetteCreateRequest(request);

        try {
            log.info("📤 Envoi vers ms-persistence avec utilisateurId: {}", request.getUtilisateurId());
            RecetteResponse response = recetteClient.createRecette(request);
            log.info("Recette créée avec succès - ID: {}", response.getId());
            log.info("📥 utilisateurId reçu de ms-persistence: {}", response.getUtilisateurId());

            // Si ms-persistence ne retourne pas utilisateurId, on le garde de la requête
            if (response.getUtilisateurId() == null && request.getUtilisateurId() != null) {
                log.warn("⚠️ PROBLÈME: ms-persistence ne retourne pas utilisateurId, on le force depuis la requête");
                response.setUtilisateurId(request.getUtilisateurId());
            }

            return response;
        } catch (Exception e) {
            log.error("Erreur lors de la création de la recette: {}", e.getMessage());
            throw new RuntimeException("Impossible de créer la recette: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "recettes", unless = "#result == null || #result.isEmpty()")
    public List<RecetteResponse> getAllRecettes() {
        log.info("Récupération de toutes les recettes");

        try {
            List<RecetteResponse> recettes = recetteClient.getAllRecettes();
            log.info("{} recettes récupérées", recettes.size());
            return recettes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes: {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer les recettes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RecetteResponse> getRecettesByUtilisateur(Long utilisateurId) {
        log.info("Récupération des recettes de l'utilisateur: {}", utilisateurId);

        try {
            List<RecetteResponse> recettes = recetteClient.getRecettesByUtilisateur(utilisateurId);
            log.info("{} recettes récupérées pour l'utilisateur {}", recettes.size(), utilisateurId);
            return recettes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer les recettes de l'utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "recette", key = "#id", unless = "#result == null")
    public RecetteResponse getRecetteById(Long id) {
        log.info("Récupération de la recette avec l'ID: {}", id);

        try {
            RecetteResponse recette = recetteClient.getRecetteById(id);
            log.info("Recette récupérée - Titre: {}", recette.getTitre());
            return recette;
        } catch (RuntimeException e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RecetteNotFoundException("Recette non trouvée avec l'ID: " + id);
        }
    }

    @Override
    public CompletableFuture<RecetteResponse> getRecetteByIdAsync(Long id) {
        log.info("Récupération asynchrone de la recette avec l'ID: {}", id);

        return recetteClient.getRecetteByIdAsync(id)
                .exceptionally(ex -> {
                    log.error("Erreur lors de la récupération asynchrone de la recette {}: {}",
                            id, ex.getMessage());
                    throw new RecetteNotFoundException("Recette non trouvée avec l'ID: " + id);
                });
    }

    @Override
    public List<RecetteResponse> searchRecettes(RecetteSearchRequest searchRequest) {
        log.info("Recherche de recettes avec critères: {}", searchRequest);

        try {
            List<RecetteResponse> allRecettes = recetteClient.getAllRecettes();
            List<RecetteResponse> filteredRecettes = allRecettes.stream()
                .filter(recette -> {
                    if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isEmpty()) {
                        String keyword = searchRequest.getKeyword().toLowerCase();
                        boolean matchTitre = recette.getTitre() != null &&
                            recette.getTitre().toLowerCase().contains(keyword);
                        boolean matchDesc = recette.getDescription() != null &&
                            recette.getDescription().toLowerCase().contains(keyword);
                        if (!matchTitre && !matchDesc) {
                            return false;
                        }
                    }

                    if (searchRequest.getCategorie() != null && !searchRequest.getCategorie().isEmpty()) {
                        if (!searchRequest.getCategorie().equals(recette.getCategorie())) {
                            return false;
                        }
                    }

                    if (searchRequest.getTempsMax() != null) {
                        if (recette.getTempsTotal() != null &&
                            recette.getTempsTotal() > searchRequest.getTempsMax()) {
                            return false;
                        }
                    }

                    if (searchRequest.getKcalMax() != null) {
                        if (recette.getKcal() != null &&
                            recette.getKcal() > searchRequest.getKcalMax()) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

            log.info("{} recettes trouvées sur {} recettes totales",
                    filteredRecettes.size(), allRecettes.size());
            return filteredRecettes;

        } catch (Exception e) {
            log.error("Erreur lors de la recherche de recettes: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "recettesByCategorie", key = "#categorie", unless = "#result == null || #result.isEmpty()")
    public List<RecetteResponse> getRecettesByCategorie(String categorie) {
        log.info("Récupération des recettes de la catégorie: {}", categorie);

        try {
            List<RecetteResponse> recettes = recetteClient.getRecettesByCategorie(categorie);
            log.info("{} recettes trouvées pour la catégorie {}", recettes.size(), categorie);
            return recettes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes par catégorie: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération par catégorie: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "recetteStats", key = "#id", unless = "#result == null")
    public RecetteStatsResponse getRecetteStats(Long id) {
        log.info("Récupération des statistiques de la recette: {}", id);

        try {
            RecetteStatsResponse stats = recetteClient.getRecetteStats(id);
            log.info("Statistiques récupérées pour la recette {} - Note moyenne: {}",
                    id, stats.getNoteMoyenne());
            return stats;
        } catch (RuntimeException e) {
            log.error("Recette non trouvée pour les stats - ID: {}", id);
            throw new RecetteNotFoundException("Recette non trouvée avec l'ID: " + id);
        }
    }

    @Override
    @CacheEvict(value = {"recette", "recettes", "recettesByCategorie", "recetteStats"},
            key = "#id", allEntries = true)
    public RecetteResponse updateRecette(Long id, RecetteUpdateRequest request) {
        log.info("Mise à jour de la recette: {}", id);
        log.info("🔍 utilisateurId reçu dans ms-recette pour mise à jour: {}", request.getUtilisateurId());

        // Valider d'abord avant toute communication avec le client
        validateRecetteUpdateRequest(request);

        try {
            log.info("📤 Envoi mise à jour vers ms-persistence avec utilisateurId: {}", request.getUtilisateurId());
            RecetteResponse response = recetteClient.updateRecette(id, request);
            log.info("Recette mise à jour avec succès - ID: {}", id);
            log.info("📥 utilisateurId reçu de ms-persistence après màj: {}", response.getUtilisateurId());

            if (response.getUtilisateurId() == null && request.getUtilisateurId() != null) {
                log.warn("⚠️ PROBLÈME: ms-persistence ne retourne pas utilisateurId après màj, on le force depuis la requête");
                response.setUtilisateurId(request.getUtilisateurId());
            }

            return response;
        } catch (RecetteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la recette: {}", e.getMessage());
            throw new RuntimeException("Impossible de mettre à jour la recette: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"recette", "recettes", "recettesByCategorie", "recetteStats"},
            key = "#id", allEntries = true)
    public void deleteRecette(Long id) {
        log.info("Suppression de la recette: {}", id);

        try {

            recetteClient.deleteRecette(id);
            log.info("Recette supprimée avec succès - ID: {}", id);
        } catch (RecetteNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la recette: {}", e.getMessage());
            throw new RuntimeException("Impossible de supprimer la recette: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean recetteExists(Long id) {
        log.debug("Vérification de l'existence de la recette: {}", id);
        return recetteClient.recetteExists(id);
    }

    @Override
    public List<RecetteResponse> getPopularRecettes(int limit) {
        log.info("Récupération des {} recettes les plus populaires", limit);

        try {
            List<RecetteResponse> allRecettes = recetteClient.getAllRecettes();

            // Trier par note moyenne (décroissant) puis par nombre de feedbacks
            return allRecettes.stream()
                    .sorted(Comparator
                            .comparing(RecetteResponse::getNoteMoyenne,
                                    Comparator.nullsLast(Comparator.reverseOrder()))
                            .thenComparing(RecetteResponse::getNombreFeedbacks,
                                    Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes populaires: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes populaires", e);
        }
    }

    @Override
    public List<RecetteResponse> getRecentRecettes(int limit) {
        log.info("Récupération des {} recettes les plus récentes", limit);

        try {
            List<RecetteResponse> allRecettes = recetteClient.getAllRecettes();

            // Trier par date de création (décroissant)
            return allRecettes.stream()
                    .sorted(Comparator.comparing(RecetteResponse::getDateCreation,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes récentes: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes récentes", e);
        }
    }

    @Override
    public List<RecetteResponse> getRecettesEnAttente() {
        log.info("Récupération des recettes en attente de validation");

        try {
            List<RecetteResponse> recettes = recetteClient.getRecettesEnAttente();
            log.info("{} recettes en attente trouvées", recettes.size());
            return recettes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes en attente: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes en attente", e);
        }
    }

    @Override
    public List<RecetteResponse> getRecettesValidees() {
        log.info("Récupération des recettes validées");

        try {
            List<RecetteResponse> recettes = recetteClient.getRecettesValidees();
            log.info("{} recettes validées trouvées", recettes.size());
            return recettes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes validées: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes validées", e);
        }
    }

    @Override
    public List<RecetteResponse> getRecettesRejetees() {
        log.info("Récupération des recettes rejetées");

        try {
            List<RecetteResponse> recettes = recetteClient.getRecettesRejetees();
            log.info("{} recettes rejetées trouvées", recettes.size());
            return recettes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes rejetées: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes rejetées", e);
        }
    }

    @Override
    @CacheEvict(value = {"recette", "recettes"}, key = "#id", allEntries = true)
    public RecetteResponse validerRecette(Long id) {
        log.info("Validation de la recette: {}", id);

        try {
            RecetteResponse response = recetteClient.validerRecette(id);
            log.info("Recette validée avec succès - ID: {}", id);
            return response;
        } catch (RuntimeException e) {
            log.error("Erreur lors de la validation de la recette: {}", e.getMessage());
            if (e.getMessage().contains("non trouvée")) {
                throw new RecetteNotFoundException("Recette non trouvée avec l'ID: " + id);
            }
            throw new RuntimeException("Impossible de valider la recette: " + e.getMessage(), e);
        }
    }

    @Override
    @CacheEvict(value = {"recette", "recettes"}, key = "#id", allEntries = true)
    public RecetteResponse rejeterRecette(Long id, String motif) {
        log.info("Rejet de la recette: {} - Motif: {}", id, motif);

        if (motif == null || motif.trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif de rejet est obligatoire");
        }

        try {
            RecetteResponse response = recetteClient.rejeterRecette(id, motif);
            log.info("Recette rejetée avec succès - ID: {}", id);
            return response;
        } catch (RuntimeException e) {
            log.error("Erreur lors du rejet de la recette: {}", e.getMessage());
            if (e.getMessage().contains("non trouvée")) {
                throw new RecetteNotFoundException("Recette non trouvée avec l'ID: " + id);
            }
            throw new RuntimeException("Impossible de rejeter la recette: " + e.getMessage(), e);
        }
    }

    // ========================================
    // MÉTHODES PRIVÉES - VALIDATION
    // ========================================

    private void validateRecetteCreateRequest(RecetteCreateRequest request) {
        if (request.getTitre() == null || request.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de la recette est obligatoire");
        }

        if (request.getIngredients() != null) {
            for (IngredientRequest ing : request.getIngredients()) {
                // Accepter soit alimentId soit alimentNom
                boolean hasAlimentId = ing.getAlimentId() != null;
                boolean hasAlimentNom = ing.getAlimentNom() != null && !ing.getAlimentNom().trim().isEmpty();

                if (!hasAlimentId && !hasAlimentNom) {
                    throw new IllegalArgumentException("L'ID ou le nom de l'aliment est requis pour chaque ingrédient");
                }
            }
        }

        if (request.getEtapes() != null) {
            for (EtapeRequest etape : request.getEtapes()) {
                if (etape.getOrdre() == null || etape.getOrdre() <= 0) {
                    throw new IllegalArgumentException("L'ordre de chaque étape doit être supérieur à 0");
                }
                if (etape.getTexte() == null || etape.getTexte().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le texte de chaque étape est obligatoire");
                }
            }
        }
    }

    private void validateRecetteUpdateRequest(RecetteUpdateRequest request) {
        if (request.getTitre() != null && request.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide");
        }

        if (request.getIngredients() != null) {
            for (IngredientRequest ing : request.getIngredients()) {
                // Accepter soit alimentId soit alimentNom
                boolean hasAlimentId = ing.getAlimentId() != null;
                boolean hasAlimentNom = ing.getAlimentNom() != null && !ing.getAlimentNom().trim().isEmpty();

                if (!hasAlimentId && !hasAlimentNom) {
                    throw new IllegalArgumentException("L'ID ou le nom de l'aliment est requis pour chaque ingrédient");
                }
            }
        }

        if (request.getEtapes() != null) {
            for (EtapeRequest etape : request.getEtapes()) {
                if (etape.getOrdre() == null || etape.getOrdre() <= 0) {
                    throw new IllegalArgumentException("L'ordre de chaque étape doit être supérieur à 0");
                }
                if (etape.getTexte() == null || etape.getTexte().trim().isEmpty()) {
                    throw new IllegalArgumentException("Le texte de chaque étape est obligatoire");
                }
            }
        }
    }
}
