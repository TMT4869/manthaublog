package com.manthau.commentservice.reaction.remove;

import com.manthau.commentservice.reaction.domain.TargetType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RemoveReactionRequest(
        @NotNull UUID targetId,
        @NotNull TargetType targetType
) {}
