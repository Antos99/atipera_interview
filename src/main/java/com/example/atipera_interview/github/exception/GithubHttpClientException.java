package com.example.atipera_interview.github.exception;

public class GithubHttpClientException extends RuntimeException {
    private static final String GITHUB_COMMUNICATION_FAILED = "Failed to communicate with the GitHub API.";

    public GithubHttpClientException(int statusCode, String message) {
        super(GITHUB_COMMUNICATION_FAILED + String.format(" Response code: %d. Response body: %s", statusCode, message));
    }

    public GithubHttpClientException(Throwable cause) {
        super(cause);
    }
}
