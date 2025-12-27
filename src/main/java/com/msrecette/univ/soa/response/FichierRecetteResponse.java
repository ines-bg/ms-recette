package com.msrecette.univ.soa.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String nomStocke;
    private String contentType;
    private Long taille;
    private String type;
    private LocalDateTime dateUpload;
    private String urlTelechargement;
}

