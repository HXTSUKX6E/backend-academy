package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotBlank;

public record RemoveLinkRequest(@NotBlank String link) {}
