package net.profitwarning.api.dto;

import java.io.Serializable;
import java.time.Instant;

public record MessageResponse(
    Long id,
    String content,
    Instant createdAt
) implements Serializable {}
