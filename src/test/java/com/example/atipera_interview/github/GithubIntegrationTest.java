package com.example.atipera_interview.github;

import com.example.atipera_interview.TestUtils;
import com.example.atipera_interview.github.dtos.ErrorResponseDto;
import com.example.atipera_interview.github.dtos.GithubBranchDto;
import com.example.atipera_interview.github.dtos.GithubRepositoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@MockServerTest("github.baseUrl=http://localhost:${mockServerPort}")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GithubIntegrationTest {
    private static final String URL_PATTERN = "http://localhost:%d%s";

    private MockServerClient mockServer;

    @LocalServerPort
    private int localPort;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper mapper;

    @Test
    void shouldReturnProperResponse() {
        // given
        String username = "testUser";
        byte[] testUserRepositories = TestUtils.getResourceAsByteArray("/testUserRepositories.json");
        byte[] repository1Branches = TestUtils.getResourceAsByteArray("/repository1Branches.json");
        byte[] repository2Branches = TestUtils.getResourceAsByteArray("/repository2Branches.json");
        mockServer.when(
                        request()
                                .withMethod("GET")
                                .withHeader("accept", "application/json")
                                .withPath("/users/{username}/repos")
                                .withPathParameter("username", username)
                                .withQueryStringParameter("type", "owner"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(testUserRepositories)
                );
        mockServer.when(
                        request()
                                .withMethod("GET")
                                .withHeader("accept", "application/json")
                                .withPath("/repos/{username}/{repository}/branches")
                                .withPathParameter("username", username)
                                .withPathParameter("repository", "repo1"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(repository1Branches)
                );
        mockServer.when(
                        request()
                                .withMethod("GET")
                                .withHeader("accept", "application/json")
                                .withPath("/repos/{username}/{repository}/branches")
                                .withPathParameter("username", username)
                                .withPathParameter("repository", "repo2"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(repository2Branches)
                );

        // when
        ResponseEntity<GithubRepositoryDto[]> response = restTemplate.getForEntity(
                String.format(URL_PATTERN, localPort, "/api/v1/github/{username}/repositories"),
                GithubRepositoryDto[].class,
                username);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(
                GithubRepositoryDto.builder()
                        .repositoryName("repo1")
                        .ownerLogin(username)
                        .branches(List.of(
                                new GithubBranchDto("master", "sha1")
                        ))
                        .build(),
                GithubRepositoryDto.builder()
                        .repositoryName("repo2")
                        .ownerLogin(username)
                        .branches(List.of(
                                new GithubBranchDto("master", "sha2"),
                                new GithubBranchDto("dev", "sha3")
                        ))
                        .build()
        );
    }

    @Test
    void shouldReturnNotFoundErrorWhenUsernameDoesNotExist() {
        // given
        String username = "nonExistentUser";
        String notFoundError = "{\"message\":\"Not Found\",\"documentation_url\":\"https://docs.github.com\"}";
        mockServer.when(
                        request()
                                .withMethod("GET")
                                .withHeader("accept", "application/json")
                                .withPath("/users/{username}/repos")
                                .withPathParameter("username", username)
                                .withQueryStringParameter("type", "owner"))
                .respond(
                        response()
                                .withStatusCode(404)
                                .withBody(notFoundError)
                );

        // when
        ResponseEntity<ErrorResponseDto> response = restTemplate.getForEntity(
                String.format(URL_PATTERN, localPort, "/api/v1/github/{username}/repositories"),
                ErrorResponseDto.class,
                username);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("User with username nonExistentUser does not exist");
    }

    @Test
    void shouldReturnGithubHttpClientExceptionWhenGithubReturnAnyErrorExceptNotFound() {
        // given
        String username = "testUser";
        String serverError = "Github crushed";
        mockServer.when(
                        request()
                                .withMethod("GET")
                                .withHeader("accept", "application/json")
                                .withPath("/users/{username}/repos")
                                .withPathParameter("username", username)
                                .withQueryStringParameter("type", "owner"))
                .respond(
                        response()
                                .withStatusCode(500)
                                .withBody(serverError)
                );

        // when
        ResponseEntity<ErrorResponseDto> response = restTemplate.getForEntity(
                String.format(URL_PATTERN, localPort, "/api/v1/github/{username}/repositories"),
                ErrorResponseDto.class,
                username);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("Failed to communicate with the GitHub API. Response code: 500. Response body: Github crushed");
    }

    @Test
    void shouldReturnJsonProcessingExceptionWhenGithubResponseHasWrongStructure() {
        // given
        String username = "testUser";
        byte[] wrongStructureJson = TestUtils.getResourceAsByteArray("/wrongStructure.json");
        mockServer.when(
                        request()
                                .withMethod("GET")
                                .withHeader("accept", "application/json")
                                .withPath("/users/{username}/repos")
                                .withPathParameter("username", username)
                                .withQueryStringParameter("type", "owner"))
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(wrongStructureJson)
                );

        // when
        ResponseEntity<ErrorResponseDto> response = restTemplate.getForEntity(
                String.format(URL_PATTERN, localPort, "/api/v1/github/{username}/repositories"),
                ErrorResponseDto.class,
                username);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("Processing response from GitHub failed. Key 'name' not found.");
    }
}
