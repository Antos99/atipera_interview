package com.example.atipera_interview.github;

import com.example.atipera_interview.github.dtos.GithubBranchDto;
import com.example.atipera_interview.github.dtos.GithubRepositoryDto;
import com.example.atipera_interview.github.exception.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GithubService {
    private static final String JSON_KEY_ERROR = "Key '%s' not found.";
    private final GithubHttpClient httpClient;
    private final ObjectMapper mapper;

    public List<GithubRepositoryDto> getRepositories(String username) {
        byte[] response = httpClient.getUserRepositories(username);
        return buildGithubRepositoryDtos(response);
    }

    private List<GithubRepositoryDto> buildGithubRepositoryDtos(byte[] repositoryResponse) {
        List<GithubRepositoryDto> githubRepositoryDtos = new ArrayList<>();
        JsonNode rootNode = getRootJsonNode(repositoryResponse);

        rootNode.forEach(repositoryNode -> {
            String repositoryName = (findObjectValueByKey(repositoryNode, "name"));
            String ownerLogin = (findObjectValueByKey(repositoryNode.get("owner"), "login"));
            boolean fork = Boolean.parseBoolean((findObjectValueByKey(repositoryNode, "fork")));
            if (!fork) {
                githubRepositoryDtos.add(GithubRepositoryDto.builder()
                        .repositoryName(repositoryName)
                        .ownerLogin(ownerLogin)
                        .build());
            }
        });

        githubRepositoryDtos.forEach(repository -> {
            byte[] branchResponse = httpClient.getRepositoryBranches(
                    repository.getOwnerLogin(), repository.getRepositoryName());
            List<GithubBranchDto> githubBranchDtos = buildGithubBranchDtos(branchResponse);
            repository.setBranches(githubBranchDtos);
        });

        return githubRepositoryDtos;
    }

    private List<GithubBranchDto> buildGithubBranchDtos(byte[] branchResponse) {
        List<GithubBranchDto> githubBranchDtos = new ArrayList<>();
        JsonNode rootNode = getRootJsonNode(branchResponse);

        rootNode.forEach(branchNode -> {
            String name = (findObjectValueByKey(branchNode, "name"));
            String lastCommitSha = (findObjectValueByKey(branchNode.get("commit"), "sha"));
            githubBranchDtos.add(new GithubBranchDto(name, lastCommitSha));
        });

        return githubBranchDtos;
    }

    private JsonNode getRootJsonNode(byte[] bytes) {
        try {
            return mapper.readTree(bytes);
        } catch (IOException e) {
            log.warn("Cannot read JSON from Github response.", e);
            throw new JsonProcessingException();
        }
    }

    private static String findObjectValueByKey(JsonNode jsonNode, String key) {
        if (jsonNode != null && !jsonNode.isNull()) {
            JsonNode nodeValue = jsonNode.get(key);
            if (nodeValue != null && !nodeValue.isNull()) {
                String stringValue = nodeValue.asText();
                if (!stringValue.isEmpty()) {
                    return stringValue;
                }
            }
        }
        throw new JsonProcessingException(String.format(JSON_KEY_ERROR, key));
    }
}
