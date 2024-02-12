package com.example.atipera_interview.github.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GithubRepositoryDto {
    private String repositoryName;
    private String ownerLogin;
    private List<GithubBranchDto> branches;
}
