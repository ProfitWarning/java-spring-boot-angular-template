package net.profitwarning.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMessageCommand(
    @NotBlank(message = "{message.content.blank}")
    String content
) {}
