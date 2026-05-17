package com.manthau.searchservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchServiceApplicationTests {

    @Test
    void applicationClassCanBeConstructed() {
        assertThat(new SearchServiceApplication()).isNotNull();
    }

}
