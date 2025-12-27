package com.msrecette.univ.soa.client;

import com.msrecette.univ.soa.request.RecetteCreateRequest;
import com.msrecette.univ.soa.request.RecetteSearchRequest;
import com.msrecette.univ.soa.request.RecetteUpdateRequest;
import com.msrecette.univ.soa.response.RecetteResponse;
import com.msrecette.univ.soa.response.RecetteStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecetteClient {

    private final RestTemplate restTemplate;

    @Value("${ms.persistance.url:http://localhost:8090}")
    private String recetteServiceUrl;

    /**
     * Créer une nouvelle recette dans le microservice Recette
     */
    public RecetteResponse createRecette(RecetteCreateRequest request) {
        String url = recetteServiceUrl + "/api/persistance/recettes";
        log.info("POST {} - Création d'une recette", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecetteCreateRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<RecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    RecetteResponse.class
            );

            log.info("Recette créée avec succès - ID: {}", response.getBody().getId());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Erreur lors de la création de la recette: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la création de la recette: " + e.getStatusCode(), e);
        }
    }

    /**
     * Récupérer toutes les recettes avec cache
     */
    @Cacheable(value = "recettes", unless = "#result == null || #result.isEmpty()")
    public List<RecetteResponse> getAllRecettes() {
        String url = recetteServiceUrl + "/api/persistance/recettes";
        log.info("GET {} - Récupération de toutes les recettes", url);

        try {
            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes récupérées", response.getBody().size());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes", e);
        }
    }

    /**
     * Récupérer les recettes d'un utilisateur
     */
    public List<RecetteResponse> getRecettesByUtilisateur(Long utilisateurId) {
        String url = recetteServiceUrl + "/api/persistance/recettes/utilisateur/" + utilisateurId;
        log.info("GET {} - Récupération des recettes de l'utilisateur {}", url, utilisateurId);

        try {
            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes récupérées pour l'utilisateur {}", response.getBody().size(), utilisateurId);
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes de l'utilisateur: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes de l'utilisateur", e);
        }
    }

    /**
     * Récupérer une recette par son ID avec cache
     */
    @Cacheable(value = "recette", key = "#id", unless = "#result == null")
    public RecetteResponse getRecetteById(Long id) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id;
        log.info("GET {} - Récupération de la recette", url);

        try {
            ResponseEntity<RecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    RecetteResponse.class
            );

            log.info("Recette récupérée - ID: {}", id);
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RuntimeException("Recette non trouvée avec l'ID: " + id);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la recette: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération de la recette", e);
        }
    }

    /**
     * Récupération asynchrone d'une recette (optimisation performance)
     */
    public CompletableFuture<RecetteResponse> getRecetteByIdAsync(Long id) {
        return CompletableFuture.supplyAsync(() -> getRecetteById(id));
    }

    /**
     * Rechercher des recettes par critères
     */
    public List<RecetteResponse> searchRecettes(RecetteSearchRequest searchRequest) {
        String url = recetteServiceUrl + "/api/persistance/recettes/search";
        log.info("POST {} - Recherche de recettes", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecetteSearchRequest> httpRequest = new HttpEntity<>(searchRequest, headers);

            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes trouvées", response.getBody().size());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la recherche de recettes: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche de recettes", e);
        }
    }

    /**
     * Récupérer des recettes par catégorie
     */
    @Cacheable(value = "recettesByCategorie", key = "#categorie", unless = "#result == null || #result.isEmpty()")
    public List<RecetteResponse> getRecettesByCategorie(String categorie) {
        String url = recetteServiceUrl + "/api/persistance/recettes/search";
        log.info("POST {} - Récupération des recettes par catégorie: {}", url, categorie);

        try {
            RecetteSearchRequest searchRequest = new RecetteSearchRequest();
            searchRequest.setCategorie(categorie);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecetteSearchRequest> httpRequest = new HttpEntity<>(searchRequest, headers);

            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpRequest,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes récupérées pour la catégorie {}", response.getBody().size(), categorie);
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes par catégorie: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes par catégorie", e);
        }
    }

    /**
     * Mettre à jour une recette
     */
    public RecetteResponse updateRecette(Long id, RecetteUpdateRequest request) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id;
        log.info("PUT {} - Mise à jour de la recette", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RecetteUpdateRequest> httpRequest = new HttpEntity<>(request, headers);

            ResponseEntity<RecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    httpRequest,
                    RecetteResponse.class
            );

            log.info("Recette mise à jour - ID: {}", id);
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RuntimeException("Recette non trouvée avec l'ID: " + id);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la recette: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour de la recette", e);
        }
    }

    /**
     * Supprimer une recette
     */
    public void deleteRecette(Long id) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id;
        log.info("DELETE {} - Suppression de la recette", url);

        try {
            restTemplate.delete(url);
            log.info("Recette supprimée - ID: {}", id);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RuntimeException("Recette non trouvée avec l'ID: " + id);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la recette: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de la recette", e);
        }
    }

    /**
     * Vérifier si une recette existe (optimisé avec HEAD request)
     */
    public boolean recetteExists(Long id) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id;
        log.debug("HEAD {} - Vérification de l'existence de la recette", url);

        try {
            HttpHeaders headers = restTemplate.headForHeaders(url);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.warn("Erreur lors de la vérification de la recette: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Récupérer les statistiques d'une recette
     */
    @Cacheable(value = "recetteStats", key = "#id", unless = "#result == null")
    public RecetteStatsResponse getRecetteStats(Long id) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id + "/stats";
        log.info("GET {} - Récupération des statistiques de la recette", url);

        try {
            ResponseEntity<RecetteStatsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    RecetteStatsResponse.class
            );

            log.info("Statistiques récupérées pour la recette - ID: {}", id);
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RuntimeException("Recette non trouvée avec l'ID: " + id);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des statistiques", e);
        }
    }

    /**
     * Récupérer les recettes en attente de validation
     */
    public List<RecetteResponse> getRecettesEnAttente() {
        String url = recetteServiceUrl + "/api/persistance/recettes/en-attente";
        log.info("GET {} - Récupération des recettes en attente", url);

        try {
            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes en attente récupérées", response.getBody().size());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes en attente: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes en attente", e);
        }
    }

    /**
     * Récupérer les recettes validées
     */
    public List<RecetteResponse> getRecettesValidees() {
        String url = recetteServiceUrl + "/api/persistance/recettes/validees";
        log.info("GET {} - Récupération des recettes validées", url);

        try {
            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes validées récupérées", response.getBody().size());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes validées: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes validées", e);
        }
    }

    /**
     * Récupérer les recettes rejetées
     */
    public List<RecetteResponse> getRecettesRejetees() {
        String url = recetteServiceUrl + "/api/persistance/recettes/rejetees";
        log.info("GET {} - Récupération des recettes rejetées", url);

        try {
            ResponseEntity<List<RecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<RecetteResponse>>() {}
            );

            log.info("{} recettes rejetées récupérées", response.getBody().size());
            return response.getBody();

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des recettes rejetées: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des recettes rejetées", e);
        }
    }

    /**
     * Valider une recette
     */
    public RecetteResponse validerRecette(Long id) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id + "/valider";
        log.info("PUT {} - Validation de la recette", url);

        try {
            ResponseEntity<RecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    null,
                    RecetteResponse.class
            );

            log.info("Recette validée - ID: {}", id);
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RuntimeException("Recette non trouvée avec l'ID: " + id);
        } catch (Exception e) {
            log.error("Erreur lors de la validation de la recette: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la validation de la recette", e);
        }
    }

    /**
     * Rejeter une recette avec un motif
     */
    public RecetteResponse rejeterRecette(Long id, String motif) {
        String url = recetteServiceUrl + "/api/persistance/recettes/" + id + "/rejeter";
        log.info("PUT {} - Rejet de la recette avec motif: {}", url, motif);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Créer un objet JSON avec le motif
            String body = "{\"motif\":\"" + motif + "\"}";
            HttpEntity<String> httpRequest = new HttpEntity<>(body, headers);

            ResponseEntity<RecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    httpRequest,
                    RecetteResponse.class
            );

            log.info("Recette rejetée - ID: {}", id);
            return response.getBody();

        } catch (HttpClientErrorException.NotFound e) {
            log.error("Recette non trouvée - ID: {}", id);
            throw new RuntimeException("Recette non trouvée avec l'ID: " + id);
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la recette: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du rejet de la recette", e);
        }
    }
}
