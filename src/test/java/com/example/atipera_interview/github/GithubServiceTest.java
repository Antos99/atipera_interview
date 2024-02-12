package com.example.atipera_interview.github;

import com.example.atipera_interview.TestUtils;
import com.example.atipera_interview.github.dtos.GithubBranchDto;
import com.example.atipera_interview.github.dtos.GithubRepositoryDto;
import com.example.atipera_interview.github.exception.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GithubServiceTest {
    @Mock
    private GithubHttpClient httpClient;

    @Spy
    private ObjectMapper mapper;

    @InjectMocks
    private GithubService githubService;

    @Test
    void shouldReturnGithubRepositoryDtos() {
        // given
        String username = "testUser";
        byte[] testUserRepositories = TestUtils.getResourceAsByteArray("/testUserRepositories.json");
        byte[] repository1Branches = TestUtils.getResourceAsByteArray("/repository1Branches.json");
        byte[] repository2Branches = TestUtils.getResourceAsByteArray("/repository2Branches.json");
        when(httpClient.getUserRepositories(username)).thenReturn(testUserRepositories);
        when(httpClient.getRepositoryBranches(username, "repo1")).thenReturn(repository1Branches);
        when(httpClient.getRepositoryBranches(username, "repo2")).thenReturn(repository2Branches);

        // when
        List<GithubRepositoryDto> repositories = githubService.getRepositories(username);

        // then
        assertThat(repositories).containsExactlyInAnyOrder(
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
    void shouldThrowJsonProcessingExceptionWhenGithubResponseHasWrongStructure() {
        // given
        String username = "testUser";
        byte[] wrongStructureJson = TestUtils.getResourceAsByteArray("/wrongStructure.json");
        when(httpClient.getUserRepositories(username)).thenReturn(wrongStructureJson);

        // when then
        JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> githubService.getRepositories(username));
        assertThat(exception.getMessage()).isEqualTo("Processing response from GitHub failed. Key 'name' not found.");
    }
}
