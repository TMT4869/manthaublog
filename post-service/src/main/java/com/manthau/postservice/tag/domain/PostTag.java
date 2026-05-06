package com.manthau.postservice.tag.domain;

import com.manthau.postservice.post.domain.Post;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_tags")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostTag {

    @EmbeddedId
    private PostTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
