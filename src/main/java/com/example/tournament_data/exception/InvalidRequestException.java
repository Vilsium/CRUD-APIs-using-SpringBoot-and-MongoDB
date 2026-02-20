package com.example.tournament_data.exception;

import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {
    // exception for bad request data, jaise ki wrong teamId

    private final String fieldName;
    private final String reason;

    public InvalidRequestException(String fieldName, String reason) {
        super(String.format("Invalid %s: %s", fieldName, reason));
        this.fieldName = fieldName;
        this.reason = reason;
    }

}
