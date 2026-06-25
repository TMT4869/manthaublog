package com.manthau.userservice.feature.profile;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostApprovedEvent {
    private UUID postId;
    private UUID authorId;
    private String authorUsername;
    private String slug;
    private String language;
    private LocalDateTime approvedAt;
}
