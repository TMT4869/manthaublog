package com.manthau.commentservice.reaction.add;

import com.manthau.commentservice.reaction.domain.ReactionType;
import com.manthau.commentservice.reaction.domain.TargetType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddReactionRequest(
        @NotNull UUID targetId,
        @NotNull TargetType targetType,
        @NotNull ReactionType type
) {}
