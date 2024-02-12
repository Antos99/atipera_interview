package com.example.atipera_interview.github.exception;

public class JsonProcessingException extends RuntimeException {
    private static final String PROCESSING_ERROR_MESSAGE = "Processing response from GitHub failed.";

    public JsonProcessingException() {
        super(PROCESSING_ERROR_MESSAGE);
    }

    public JsonProcessingException(String message) {
        super(PROCESSING_ERROR_MESSAGE + " " + message);
    }
}
