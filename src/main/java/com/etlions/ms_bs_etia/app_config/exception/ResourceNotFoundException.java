package com.etlions.ms_bs_etia.app_config.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " no encontrado con ID: " + id);
    }
}

