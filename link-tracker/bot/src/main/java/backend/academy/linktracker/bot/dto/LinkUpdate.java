package backend.academy.linktracker.bot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdate(
        @NotNull Long id,
        @NotBlank String url,
        @NotBlank String description,
        @NotNull List<Long> tgChatIds,
        List<String> tags) {}
