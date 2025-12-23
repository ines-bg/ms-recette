package com.springbootTemplate.univ.soa.service;

import com.springbootTemplate.univ.soa.client.RecetteClient;
import com.springbootTemplate.univ.soa.exception.RecetteNotFoundException;
import com.springbootTemplate.univ.soa.request.RecetteCreateRequest;
import com.springbootTemplate.univ.soa.request.RecetteSearchRequest;
import com.springbootTemplate.univ.soa.request.RecetteUpdateRequest;
import com.springbootTemplate.univ.soa.response.RecetteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - RecetteServiceImpl")
class RecetteServiceImplTest {

    @Mock
    private RecetteClient recetteClient;

    @InjectMocks
    private RecetteServiceImpl recetteService;

    private RecetteResponse recetteResponse;
    private RecetteCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        recetteResponse = new RecetteResponse();
        recetteResponse.setId(1L);
        recetteResponse.setTitre("Test Recette");
        recetteResponse.setDescription("Description test");
        recetteResponse.setDifficulte("FACILE");
        recetteResponse.setCategorie("PLAT_PRINCIPAL");
        recetteResponse.setTempsTotal(35);
        recetteResponse.setKcal(450);

        createRequest = new RecetteCreateRequest();
        createRequest.setTitre("Nouvelle Recette");
        createRequest.setDescription("Description");
    }

    @Test
    @DisplayName("createRecette - devrait créer une recette avec succès")
    void testCreateRecette_Success() {
        when(recetteClient.createRecette(any(RecetteCreateRequest.class)))
                .thenReturn(recetteResponse);

        RecetteResponse result = recetteService.createRecette(createRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Recette", result.getTitre());
        verify(recetteClient, times(1)).createRecette(any(RecetteCreateRequest.class));
    }

    @Test
    @DisplayName("createRecette - devrait lancer une exception en cas d'erreur")
    void testCreateRecette_ThrowsException() {
        when(recetteClient.createRecette(any(RecetteCreateRequest.class)))
                .thenThrow(new RuntimeException("Erreur création"));

        assertThrows(RuntimeException.class, () -> {
            recetteService.createRecette(createRequest);
        });
    }

    @Test
    @DisplayName("getAllRecettes - devrait retourner la liste des recettes")
    void testGetAllRecettes_Success() {
        List<RecetteResponse> recettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(recettes);

        List<RecetteResponse> result = recetteService.getAllRecettes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Recette", result.get(0).getTitre());
        verify(recetteClient, times(1)).getAllRecettes();
    }

    @Test
    @DisplayName("getRecetteById - devrait retourner une recette par ID")
    void testGetRecetteById_Success() {
        when(recetteClient.getRecetteById(1L)).thenReturn(recetteResponse);

        RecetteResponse result = recetteService.getRecetteById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Recette", result.getTitre());
        verify(recetteClient, times(1)).getRecetteById(1L);
    }

    @Test
    @DisplayName("getRecetteById - devrait lancer RecetteNotFoundException si non trouvée")
    void testGetRecetteById_NotFound() {
        when(recetteClient.getRecetteById(999L))
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(RecetteNotFoundException.class, () -> {
            recetteService.getRecetteById(999L);
        });
    }

    @Test
    @DisplayName("searchRecettes - devrait filtrer par catégorie")
    void testSearchRecettes_ByCategorie() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setCategorie("PLAT_PRINCIPAL");

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PLAT_PRINCIPAL", result.get(0).getCategorie());
    }

    @Test
    @DisplayName("searchRecettes - devrait filtrer par keyword")
    void testSearchRecettes_ByKeyword() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setKeyword("Test");

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitre().contains("Test"));
    }

    @Test
    @DisplayName("searchRecettes - devrait filtrer par temps max")
    void testSearchRecettes_ByTempsMax() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setTempsMax(40);

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTempsTotal() <= 40);
    }

    @Test
    @DisplayName("searchRecettes - devrait retourner liste vide si aucun résultat")
    void testSearchRecettes_EmptyResult() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setCategorie("DESSERT");

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("updateRecette - devrait mettre à jour une recette")
    void testUpdateRecette_Success() {
        RecetteUpdateRequest updateRequest = new RecetteUpdateRequest();
        updateRequest.setTitre("Titre modifié");

        when(recetteClient.recetteExists(1L)).thenReturn(true);
        when(recetteClient.updateRecette(anyLong(), any(RecetteUpdateRequest.class)))
                .thenReturn(recetteResponse);

        RecetteResponse result = recetteService.updateRecette(1L, updateRequest);

        assertNotNull(result);
        verify(recetteClient, times(1)).recetteExists(1L);
        verify(recetteClient, times(1)).updateRecette(eq(1L), any(RecetteUpdateRequest.class));
    }

    @Test
    @DisplayName("deleteRecette - devrait supprimer une recette")
    void testDeleteRecette_Success() {
        when(recetteClient.recetteExists(1L)).thenReturn(true);
        doNothing().when(recetteClient).deleteRecette(1L);

        assertDoesNotThrow(() -> recetteService.deleteRecette(1L));
        verify(recetteClient, times(1)).recetteExists(1L);
        verify(recetteClient, times(1)).deleteRecette(1L);
    }

    @Test
    @DisplayName("recetteExists - devrait retourner true si recette existe")
    void testRecetteExists_True() {
        when(recetteClient.recetteExists(1L)).thenReturn(true);

        boolean result = recetteService.recetteExists(1L);

        assertTrue(result);
        verify(recetteClient, times(1)).recetteExists(1L);
    }

    @Test
    @DisplayName("recetteExists - devrait retourner false si recette n'existe pas")
    void testRecetteExists_False() {
        when(recetteClient.recetteExists(999L)).thenReturn(false);

        boolean result = recetteService.recetteExists(999L);

        assertFalse(result);
        verify(recetteClient, times(1)).recetteExists(999L);
    }

    @Test
    @DisplayName("getRecettesByCategorie - devrait retourner recettes par catégorie")
    void testGetRecettesByCategorie_Success() {
        List<RecetteResponse> recettes = Arrays.asList(recetteResponse);
        when(recetteClient.getRecettesByCategorie("PLAT_PRINCIPAL")).thenReturn(recettes);

        List<RecetteResponse> result = recetteService.getRecettesByCategorie("PLAT_PRINCIPAL");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recetteClient, times(1)).getRecettesByCategorie("PLAT_PRINCIPAL");
    }

    @Test
    @DisplayName("deleteRecette - devrait lancer RecetteNotFoundException si non trouvée")
    void testDeleteRecette_NotFound() {
        when(recetteClient.recetteExists(999L)).thenReturn(false);

        assertThrows(Exception.class, () -> recetteService.deleteRecette(999L));
        verify(recetteClient, times(1)).recetteExists(999L);
        verify(recetteClient, never()).deleteRecette(999L);
    }

    @Test
    @DisplayName("updateRecette - devrait lancer RecetteNotFoundException si non trouvée")
    void testUpdateRecette_NotFound() {
        RecetteUpdateRequest updateRequest = new RecetteUpdateRequest();
        updateRequest.setTitre("Titre modifié");

        when(recetteClient.recetteExists(999L)).thenReturn(false);

        assertThrows(Exception.class, () -> recetteService.updateRecette(999L, updateRequest));
        verify(recetteClient, times(1)).recetteExists(999L);
        verify(recetteClient, never()).updateRecette(eq(999L), any());
    }

    @Test
    @DisplayName("getRecettesByCategorie - devrait lancer exception en cas d'erreur")
    void testGetRecettesByCategorie_ThrowsException() {
        when(recetteClient.getRecettesByCategorie("INVALID"))
                .thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class, () ->
            recetteService.getRecettesByCategorie("INVALID"));
    }

    @Test
    @DisplayName("getRecetteByIdAsync - devrait retourner CompletableFuture avec recette")
    void testGetRecetteByIdAsync_Success() throws Exception {
        when(recetteClient.getRecetteByIdAsync(1L))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(recetteResponse));

        java.util.concurrent.CompletableFuture<RecetteResponse> future =
            recetteService.getRecetteByIdAsync(1L);

        assertNotNull(future);
        RecetteResponse result = future.get();
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("searchRecettes - devrait filtrer par temps max et calories max")
    void testSearchRecettes_ByTempsMaxAndKcalMax() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setTempsMax(40);
        searchRequest.setKcalMax(500);

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchRecettes - devrait retourner liste vide si temps max dépassé")
    void testSearchRecettes_TempsMaxExceeded() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setTempsMax(10); // Moins que le temps total (35)

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("searchRecettes - devrait retourner liste vide si calories max dépassées")
    void testSearchRecettes_KcalMaxExceeded() {
        RecetteSearchRequest searchRequest = new RecetteSearchRequest();
        searchRequest.setKcalMax(400); // Moins que kcal (450)

        List<RecetteResponse> allRecettes = Arrays.asList(recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.searchRecettes(searchRequest);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("getRecettesEnAttente - devrait retourner la liste des recettes en attente")
    void testGetRecettesEnAttente_Success() {
        recetteResponse.setStatut(com.springbootTemplate.univ.soa.response.StatutRecetteEnum.EN_ATTENTE);
        List<RecetteResponse> recettes = Arrays.asList(recetteResponse);
        when(recetteClient.getRecettesEnAttente()).thenReturn(recettes);

        List<RecetteResponse> result = recetteService.getRecettesEnAttente();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(com.springbootTemplate.univ.soa.response.StatutRecetteEnum.EN_ATTENTE, result.get(0).getStatut());
        verify(recetteClient, times(1)).getRecettesEnAttente();
    }

    @Test
    @DisplayName("getRecettesEnAttente - devrait lancer une exception en cas d'erreur")
    void testGetRecettesEnAttente_ThrowsException() {
        when(recetteClient.getRecettesEnAttente())
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(RuntimeException.class, () -> recetteService.getRecettesEnAttente());
    }

    @Test
    @DisplayName("validerRecette - devrait valider une recette avec succès")
    void testValiderRecette_Success() {
        recetteResponse.setActif(true);
        recetteResponse.setStatut(com.springbootTemplate.univ.soa.response.StatutRecetteEnum.VALIDEE);

        when(recetteClient.validerRecette(1L)).thenReturn(recetteResponse);

        RecetteResponse result = recetteService.validerRecette(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getActif());
        assertEquals(com.springbootTemplate.univ.soa.response.StatutRecetteEnum.VALIDEE, result.getStatut());
        verify(recetteClient, times(1)).validerRecette(1L);
    }

    @Test
    @DisplayName("validerRecette - devrait lancer RecetteNotFoundException si recette non trouvée")
    void testValiderRecette_NotFound() {
        when(recetteClient.validerRecette(999L))
                .thenThrow(new RuntimeException("Recette non trouvée"));

        assertThrows(RecetteNotFoundException.class, () -> recetteService.validerRecette(999L));
    }

    @Test
    @DisplayName("rejeterRecette - devrait rejeter une recette avec succès")
    void testRejeterRecette_Success() {
        String motif = "Recette incomplète";
        recetteResponse.setActif(false);
        recetteResponse.setStatut(com.springbootTemplate.univ.soa.response.StatutRecetteEnum.REJETEE);
        recetteResponse.setMotifRejet(motif);

        when(recetteClient.rejeterRecette(1L, motif)).thenReturn(recetteResponse);

        RecetteResponse result = recetteService.rejeterRecette(1L, motif);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertFalse(result.getActif());
        assertEquals(com.springbootTemplate.univ.soa.response.StatutRecetteEnum.REJETEE, result.getStatut());
        assertEquals(motif, result.getMotifRejet());
        verify(recetteClient, times(1)).rejeterRecette(1L, motif);
    }

    @Test
    @DisplayName("rejeterRecette - devrait lancer RecetteNotFoundException si recette non trouvée")
    void testRejeterRecette_NotFound() {
        when(recetteClient.rejeterRecette(999L, "Motif"))
                .thenThrow(new RuntimeException("Recette non trouvée"));

        assertThrows(RecetteNotFoundException.class, () ->
            recetteService.rejeterRecette(999L, "Motif"));
    }

    @Test
    @DisplayName("rejeterRecette - devrait lancer IllegalArgumentException si motif vide")
    void testRejeterRecette_EmptyMotif() {
        assertThrows(IllegalArgumentException.class, () ->
            recetteService.rejeterRecette(1L, ""));
    }

    @Test
    @DisplayName("rejeterRecette - devrait lancer IllegalArgumentException si motif null")
    void testRejeterRecette_NullMotif() {
        assertThrows(IllegalArgumentException.class, () ->
            recetteService.rejeterRecette(1L, null));
    }

    @Test
    @DisplayName("getPopularRecettes - devrait retourner recettes populaires triées")
    void testGetPopularRecettes_Success() {
        RecetteResponse recette2 = new RecetteResponse();
        recette2.setId(2L);
        recette2.setTitre("Recette 2");
        recette2.setNoteMoyenne(4.8);
        recette2.setNombreFeedbacks(50);

        recetteResponse.setNoteMoyenne(4.5);
        recetteResponse.setNombreFeedbacks(30);

        List<RecetteResponse> allRecettes = Arrays.asList(recette2, recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.getPopularRecettes(2);

        assertNotNull(result);
        assertEquals(2, result.size());
        // La première devrait être celle avec la meilleure note
        assertEquals(4.8, result.get(0).getNoteMoyenne());
    }

    @Test
    @DisplayName("getRecentRecettes - devrait retourner recettes récentes triées")
    void testGetRecentRecettes_Success() {
        RecetteResponse recette2 = new RecetteResponse();
        recette2.setId(2L);
        recette2.setTitre("Recette 2");
        recette2.setDateCreation(java.time.LocalDateTime.now().minusDays(1));

        recetteResponse.setDateCreation(java.time.LocalDateTime.now());

        List<RecetteResponse> allRecettes = Arrays.asList(recette2, recetteResponse);
        when(recetteClient.getAllRecettes()).thenReturn(allRecettes);

        List<RecetteResponse> result = recetteService.getRecentRecettes(2);

        assertNotNull(result);
        assertEquals(2, result.size());
        // La première devrait être la plus récente
        assertEquals(recetteResponse.getId(), result.get(0).getId());
    }
}

