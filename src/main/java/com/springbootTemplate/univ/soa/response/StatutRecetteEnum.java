package com.springbootTemplate.univ.soa.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut d'une recette côté persistance")
public enum StatutRecetteEnum {
    EN_ATTENTE,
    VALIDEE,
    REJETEE
}

