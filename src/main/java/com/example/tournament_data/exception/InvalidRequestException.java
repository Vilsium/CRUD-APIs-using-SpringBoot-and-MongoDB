package com.example.tournament_data.exception;

public class InvalidRequestException extends RuntimeException {
    // exception for bad request data, jaise ki wrong teamId

    private String fieldName;
    private String reason;

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String fieldName, String reason) {
        super(String.format("Invalid %s: %s", fieldName, reason));
        this.fieldName = fieldName;
        this.reason = reason;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getReason() {
        return reason;
    }
}
