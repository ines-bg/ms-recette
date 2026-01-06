package com.msrecette.univ.soa.client;


import com.msrecette.univ.soa.response.FichierRecetteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FichierRecetteClient {

    private final RestTemplate restTemplate;

    @Value("${ms.persistance.url:http://localhost:8090}")
    private String persistanceServiceUrl;

    public FichierRecetteResponse uploadImage(Long recetteId, MultipartFile file) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/images";
        log.info("POST {} - Upload image", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FichierRecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    FichierRecetteResponse.class
            );

            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier", e);
        } catch (HttpStatusCodeException e) {
            log.error("Upload image failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de l'upload de l'image: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Upload image timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de l'upload de l'image", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }

    public FichierRecetteResponse uploadDocument(Long recetteId, MultipartFile file) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/documents";
        log.info("POST {} - Upload document", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FichierRecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    FichierRecetteResponse.class
            );

            return response.getBody();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier", e);
        } catch (HttpStatusCodeException e) {
            log.error("Upload document failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de l'upload du document: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Upload document timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de l'upload du document", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'upload du document", e);
        }
    }

    public List<FichierRecetteResponse> getFichiersByRecette(Long recetteId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers";
        log.info("GET {}", url);

        try {
            ResponseEntity<List<FichierRecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FichierRecetteResponse>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Get fichiers failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la récupération des fichiers: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Get fichiers timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de la récupération des fichiers", e);
        }
    }

    public List<FichierRecetteResponse> getImagesByRecette(Long recetteId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/images";
        log.info("GET {}", url);

        try {
            ResponseEntity<List<FichierRecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FichierRecetteResponse>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Get images failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la récupération des images: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Get images timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de la récupération des images", e);
        }
    }

    public List<FichierRecetteResponse> getDocumentsByRecette(Long recetteId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/documents";
        log.info("GET {}", url);

        try {
            ResponseEntity<List<FichierRecetteResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<FichierRecetteResponse>>() {}
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Get documents failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la récupération des documents: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Get documents timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de la récupération des documents", e);
        }
    }

    public ResponseEntity<Resource> downloadFichier(Long recetteId, Long fichierId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/" + fichierId + "/download";
        log.info("GET {}", url);

        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Resource.class
            );
        } catch (HttpStatusCodeException e) {
            log.error("Download failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors du téléchargement du fichier: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Download timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors du téléchargement du fichier", e);
        }
    }

    public FichierRecetteResponse getFichierMetadata(Long recetteId, Long fichierId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/" + fichierId;
        log.info("GET {}", url);

        try {
            ResponseEntity<FichierRecetteResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    FichierRecetteResponse.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("Get metadata failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la récupération des métadonnées: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Get metadata timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de la récupération des métadonnées", e);
        }
    }

    public void deleteFichier(Long recetteId, Long fichierId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/" + fichierId;
        log.info("DELETE {}", url);
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            log.error("Delete fichier failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Delete fichier timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de la suppression du fichier", e);
        }
    }

    public void deleteAllFichiersByRecette(Long recetteId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers";
        log.info("DELETE {}", url);
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            log.error("Delete all fichiers failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors de la suppression de tous les fichiers: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Delete all fichiers timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors de la suppression de tous les fichiers", e);
        }
    }

    public ResponseEntity<Resource> streamImage(Long recetteId, Long fichierId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/images/" + fichierId + "/content";
        log.info("GET {} - Stream image", url);

        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Resource.class
            );
        } catch (HttpStatusCodeException e) {
            log.error("Stream image failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors du streaming de l'image: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Stream image timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors du streaming de l'image", e);
        }
    }

    public ResponseEntity<Resource> streamAny(Long recetteId, Long fichierId) {
        String url = persistanceServiceUrl + "/api/persistance/recettes/" + recetteId + "/fichiers/" + fichierId + "/content";
        log.info("GET {} - Stream fichier", url);

        try {
            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Resource.class
            );
        } catch (HttpStatusCodeException e) {
            log.error("Stream fichier failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erreur lors du streaming du fichier: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("Stream fichier timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Timeout/connexion ms-persistance lors du streaming du fichier", e);
        }
    }

    private static class MultipartInputStreamFileResource extends InputStreamResource {
        private final String filename;

        public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
            super(inputStream);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

        @Override
        public long contentLength() throws IOException {
            return -1;
        }
    }
}

