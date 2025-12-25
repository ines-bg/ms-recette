package com.msrecette.univ.soa.exception;

public class RecetteNotFoundException extends RuntimeException {
    public RecetteNotFoundException(String message) {
        super(message);
    }
}

