package com.manthau.postservice.category.admin;

import com.manthau.postservice.category.domain.Category;
import com.manthau.postservice.category.domain.CategoryRepository;
import com.manthau.postservice.category.dto.CategoryDto;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryHandlerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private AdminCategoryHandler handler;

    @Test
    void createGeneratesSlugAndNormalizesInput() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryDto result = handler.create(new CategoryRequest(" Backend ", null, " APIs "));

        assertThat(result.name()).isEqualTo("Backend");
        assertThat(result.slug()).isEqualTo("backend");
        assertThat(result.description()).isEqualTo("APIs");
        verify(categoryRepository).existsByNameIgnoreCase("Backend");
        verify(categoryRepository).existsBySlug("backend");
    }

    @Test
    void createRejectsDuplicateSlug() {
        when(categoryRepository.existsBySlug("backend")).thenReturn(true);
        CategoryRequest request = new CategoryRequest("Backend", "backend", null);

        assertThatThrownBy(() -> handler.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Category slug already exists");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteRejectsCategoryUsedByPosts() {
        Category category = Category.builder()
                .name("Backend")
                .slug("backend")
                .build();
        when(categoryRepository.findBySlug("backend")).thenReturn(Optional.of(category));
        when(postRepository.existsByCategorySlug("backend")).thenReturn(true);
        String slug = "backend";

        assertThatThrownBy(() -> handler.delete(slug))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Category is used by posts");

        verify(categoryRepository, never()).delete(any());
    }
}
