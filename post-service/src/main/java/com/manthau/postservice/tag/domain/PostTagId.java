package com.manthau.postservice.tag.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PostTagId implements Serializable {

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "tag_id")
    private UUID tagId;
}
