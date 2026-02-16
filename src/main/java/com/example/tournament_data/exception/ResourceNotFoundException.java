package com.example.tournament_data.exception;

public class ResourceNotFoundException extends RuntimeException {
    // will handle exception for missing Player or missing Team

    private String resourceName;
    private String fieldName;
    private Integer fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Integer id) {
        super(String.format("%s was not found with %s: %s", resourceName, fieldName, id));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = id;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Integer getFieldValue() {
        return fieldValue;
    }
}
