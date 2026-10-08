package backend.academy.linktracker.scrapper.dto;

import jakarta.validation.constraints.NotBlank;

public record RemoveLinkRequest(@NotBlank String link) {}
