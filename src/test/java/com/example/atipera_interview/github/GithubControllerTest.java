package com.example.atipera_interview.github;

import com.example.atipera_interview.github.dtos.GithubBranchDto;
import com.example.atipera_interview.github.dtos.GithubRepositoryDto;
import com.example.atipera_interview.github.exception.GithubHttpClientException;
import com.example.atipera_interview.github.exception.JsonProcessingException;
import com.example.atipera_interview.github.exception.NotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubController.class)
public class GithubControllerTest {
    private final static String GET_REPOSITORIES_URL = "/api/v1/github/{username}/repositories";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GithubService githubService;

    @Test
    void shouldReturnProperResponse() throws Exception {
        // given
        String username = "testUser";
        List<GithubRepositoryDto> expectedResult = List.of(
                GithubRepositoryDto.builder()
                        .repositoryName("repo1")
                        .ownerLogin(username)
                        .branches(List.of(
                                new GithubBranchDto("master", "sha1"),
                                new GithubBranchDto("dev", "sha2")
                        ))
                        .build(),
                GithubRepositoryDto.builder()
                        .repositoryName("repo2")
                        .ownerLogin(username)
                        .branches(List.of(
                                new GithubBranchDto("master", "sha3"),
                                new GithubBranchDto("test", "sha4")
                        ))
                        .build()
        );
        when(githubService.getRepositories(username)).thenReturn(expectedResult);

        // when
        ResultActions result = this.mockMvc.perform(get(GET_REPOSITORIES_URL, username));
        String x = this.mockMvc.perform(get(GET_REPOSITORIES_URL, username)).andReturn().getResponse().getContentAsString();
        System.out.println(x);

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].repositoryName", Matchers.is("repo1")))
                .andExpect(jsonPath("$[0].ownerLogin", Matchers.is(username)))
                .andExpect(jsonPath("$[0].branches[0].name", Matchers.is("master")))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha", Matchers.is("sha1")))
                .andExpect(jsonPath("$[0].branches[1].name", Matchers.is("dev")))
                .andExpect(jsonPath("$[0].branches[1].lastCommitSha", Matchers.is("sha2")))
                .andExpect(jsonPath("$[1].repositoryName", Matchers.is("repo2")))
                .andExpect(jsonPath("$[1].ownerLogin", Matchers.is(username)))
                .andExpect(jsonPath("$[1].branches[0].name", Matchers.is("master")))
                .andExpect(jsonPath("$[1].branches[0].lastCommitSha", Matchers.is("sha3")))
                .andExpect(jsonPath("$[1].branches[1].name", Matchers.is("test")))
                .andExpect(jsonPath("$[1].branches[1].lastCommitSha", Matchers.is("sha4")));
    }

    @Test
    void shouldHandleNotFoundException() throws Exception {
        // given
        String username = "testUser";
        when(githubService.getRepositories(username)).thenThrow(new NotFoundException(username));

        // when
        ResultActions result = this.mockMvc.perform(get(GET_REPOSITORIES_URL, username));

        // then
        result
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", Matchers.is(404)))
                .andExpect(jsonPath("$.message", Matchers.is("User with username testUser does not exist")));
    }

    @Test
    void shouldHandleJsonProcessingException() throws Exception {
        // given
        String username = "testUser";
        when(githubService.getRepositories(username)).thenThrow(new JsonProcessingException());

        // when
        ResultActions result = this.mockMvc.perform(get(GET_REPOSITORIES_URL, username));

        // then
        result
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", Matchers.is(500)))
                .andExpect(jsonPath("$.message", Matchers.is("Processing response from GitHub failed.")));
    }

    @Test
    void shouldHandleGithubHttpClientException() throws Exception {
        // given
        String username = "testUser";
        when(githubService.getRepositories(username)).thenThrow(new GithubHttpClientException(500, "GitHub crushed"));

        // when
        ResultActions result = this.mockMvc.perform(get(GET_REPOSITORIES_URL, username));

        // then
        result
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", Matchers.is(500)))
                .andExpect(jsonPath("$.message", Matchers.is("Failed to communicate with the GitHub API. Response code: 500. Response body: GitHub crushed")));
    }
}
