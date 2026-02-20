package com.example.tournament_data.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    // will handle exception for missing Player or missing Team

    private final String resourceName;
    private final String fieldName;
    private final Integer fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Integer id) {
        super(String.format("%s was not found with %s: %s", resourceName, fieldName, id));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = id;
    }

}
