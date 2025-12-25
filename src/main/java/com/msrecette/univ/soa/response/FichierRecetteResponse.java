package com.msrecette.univ.soa.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FichierRecetteResponse {
    private Long id;
    private Long recetteId;
    private String nomOriginal;

    @JsonProperty("nomStocke")  // ms-persistance utilise "nomStocke"
    private String nomStockage;

    private String contentType;
    private Long taille;
    private String type;

    @JsonProperty("cheminMinio")  // ms-persistance utilise "cheminMinio"
    private String url;

    @JsonProperty("dateUpload")  // ms-persistance utilise "dateUpload"
    private LocalDateTime dateCreation;
}

