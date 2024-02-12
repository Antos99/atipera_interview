package com.example.atipera_interview.github.exception;

public class NotFoundException extends RuntimeException{
    private static final String NOT_FOUND_MESSAGE = "User with username %s does not exist";

    public NotFoundException(String username) {
        super(String.format(NOT_FOUND_MESSAGE, username));
    }
}
