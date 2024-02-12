package com.example.atipera_interview.github.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GithubBranchDto {
    private String name;
    private String lastCommitSha;
}
