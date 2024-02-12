package com.example.atipera_interview.github;

import com.example.atipera_interview.github.dtos.ErrorResponseDto;
import com.example.atipera_interview.github.dtos.GithubRepositoryDto;
import com.example.atipera_interview.github.exception.GithubHttpClientException;
import com.example.atipera_interview.github.exception.JsonProcessingException;
import com.example.atipera_interview.github.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/github")
public class GithubController {
    private final GithubService githubService;

    @GetMapping(value = "/{username}/repositories", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GithubRepositoryDto>> getRepositories(@PathVariable String username) {
        log.debug("Getting repositories for user {}", username);
        return ResponseEntity.ok(githubService.getRepositories(username));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponseDto> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new ErrorResponseDto(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(JsonProcessingException.class)
    ResponseEntity<ErrorResponseDto> handleJsonProcessingException(JsonProcessingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(GithubHttpClientException.class)
    ResponseEntity<ErrorResponseDto> handleGithubHttpClientException(GithubHttpClientException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }
}
