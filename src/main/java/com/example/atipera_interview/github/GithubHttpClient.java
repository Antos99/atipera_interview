package com.example.atipera_interview.github;

import com.example.atipera_interview.github.exception.GithubHttpClientException;
import com.example.atipera_interview.github.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class GithubHttpClient {
    private static final String USER_REPOS_URL = "/users/{username}/repos";
    private static final String REPO_BRANCHES_URL = "/repos/{username}/{repository}/branches";

    private final RestClient restClient;

    public GithubHttpClient(@Value("${github.baseUrl}") String githubBaseUrl) {
        restClient = RestClient.builder().baseUrl(githubBaseUrl).build();
    }

    public byte[] getUserRepositories(String username) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_REPOS_URL)
                        .queryParam("type", "owner")
                        .build(username))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                    log.warn("User {} not found in Github", username);
                    throw new NotFoundException(username);
                })
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    GithubHttpClientException e = new GithubHttpClientException(res.getStatusCode().value(), mapResponseToString(res));
                    log.warn("Unexpected error from Github API.", e);
                    throw e;
                })
                .body(byte[].class);
    }

    public byte[] getRepositoryBranches(String username, String repository) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(REPO_BRANCHES_URL)
                        .build(username, repository))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    GithubHttpClientException e = new GithubHttpClientException(res.getStatusCode().value(), mapResponseToString(res));
                    log.warn("Unexpected error from Github API.", e);
                    throw e;
                })
                .body(byte[].class);
    }

    private static String mapResponseToString(ClientHttpResponse response) {
        try {
            return new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Cannot read body from Github API response.", e);
            throw new GithubHttpClientException(e);
        }
    }
}
