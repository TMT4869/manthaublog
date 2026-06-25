package com.manthau.commentservice.reaction.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReactionId implements Serializable {

    private UUID userId;
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    private TargetType targetType;
}
