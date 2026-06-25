package com.manthau.postservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostServiceApplicationTests {

    @Test
    void applicationClassCanBeConstructed() {
        assertThat(new PostServiceApplication()).isNotNull();
    }

}
