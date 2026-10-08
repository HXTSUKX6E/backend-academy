package backend.academy.linktracker.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubIssueDto(
        String title, @JsonProperty("created_at") String createdAt, GithubUserDto user, String body) {

    public record GithubUserDto(String login) {}
}
