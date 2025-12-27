package com.msrecette.univ.soa.controller;

import com.msrecette.univ.soa.request.RecetteSearchRequest;
import com.msrecette.univ.soa.response.RecetteResponse;
import com.msrecette.univ.soa.service.RecetteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetteController.class)
@DisplayName("Tests additionnels - RecetteController")
class RecetteControllerAdditionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecetteService recetteService;

    private RecetteResponse recetteResponse;

    @BeforeEach
    void setUp() {
        recetteResponse = new RecetteResponse();
        recetteResponse.setId(1L);
        recetteResponse.setTitre("Test Recette");
        recetteResponse.setDifficulte("FACILE");
        recetteResponse.setCategorie("PLAT_PRINCIPAL");
    }

    @Test
    @DisplayName("GET /api/recettes/recentes - devrait retourner les recettes récentes")
    void testGetRecettesRecentes() throws Exception {
        when(recetteService.searchRecettes(any(RecetteSearchRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recettes/recentes")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/recettes/populaires - devrait retourner les recettes populaires")
    void testGetRecettesPopulaires() throws Exception {
        when(recetteService.searchRecettes(any(RecetteSearchRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recettes/populaires")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/recettes/{id}/async - devrait retourner une recette en mode async")
    void testGetRecetteByIdAsync() throws Exception {
        when(recetteService.getRecetteByIdAsync(1L))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(recetteResponse));

        mockMvc.perform(get("/api/recettes/1/async"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/recettes/{id}/stats - devrait retourner les statistiques d'une recette")
    void testGetRecetteStats() throws Exception {
        mockMvc.perform(get("/api/recettes/1/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/recettes/{id}/valider - devrait valider une recette")
    void testValiderRecette() throws Exception {
        recetteResponse.setStatut("VALIDEE");
        recetteResponse.setActif(true);
        when(recetteService.validerRecette(1L)).thenReturn(recetteResponse);

        mockMvc.perform(put("/api/recettes/1/valider"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("VALIDEE"));
    }

    @Test
    @DisplayName("PUT /api/recettes/{id}/rejeter - devrait rejeter une recette")
    void testRejeterRecette() throws Exception {
        recetteResponse.setStatut("REJETEE");
        recetteResponse.setMotifRejet("Non conforme");
        when(recetteService.rejeterRecette(anyLong(), anyString()))
                .thenReturn(recetteResponse);

        String requestBody = """
                {
                  "motif": "Non conforme"
                }
                """;

        mockMvc.perform(put("/api/recettes/1/rejeter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("REJETEE"))
                .andExpect(jsonPath("$.motifRejet").value("Non conforme"));
    }

    @Test
    @DisplayName("GET /api/recettes/en-attente - devrait retourner les recettes en attente")
    void testGetRecettesEnAttente() throws Exception {
        List<RecetteResponse> recettes = Arrays.asList(recetteResponse);
        when(recetteService.searchRecettes(any(RecetteSearchRequest.class)))
                .thenReturn(recettes);

        mockMvc.perform(get("/api/recettes/en-attente"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/recettes/search - recherche avec liste vide")
    void testSearchRecettes_EmptyResult() throws Exception {
        when(recetteService.searchRecettes(any(RecetteSearchRequest.class)))
                .thenReturn(Collections.emptyList());

        String searchBody = """
                {
                  "keyword": "inexistant"
                }
                """;

        mockMvc.perform(post("/api/recettes/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/recettes - devrait retourner liste vide si aucune recette")
    void testGetAllRecettes_EmptyList() throws Exception {
        when(recetteService.getAllRecettes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recettes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /api/recettes/categorie/{categorie} - liste vide")
    void testGetRecettesByCategorie_EmptyList() throws Exception {
        when(recetteService.getRecettesByCategorie(anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/recettes/categorie/DESSERT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}

