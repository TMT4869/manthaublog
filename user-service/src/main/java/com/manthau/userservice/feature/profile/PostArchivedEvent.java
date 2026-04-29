package com.manthau.userservice.feature.profile;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostArchivedEvent {
    private UUID postId;
    private UUID authorId;
    private String authorUsername;
    private LocalDateTime archivedAt;
}
