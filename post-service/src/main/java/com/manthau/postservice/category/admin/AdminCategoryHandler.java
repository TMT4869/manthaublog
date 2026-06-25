package com.manthau.postservice.category.admin;

import com.manthau.postservice.category.domain.Category;
import com.manthau.postservice.category.domain.CategoryRepository;
import com.manthau.postservice.category.dto.CategoryDto;
import com.manthau.postservice.post.domain.PostRepository;
import com.manthau.postservice.shared.exception.BadRequestException;
import com.manthau.postservice.shared.exception.ConflictException;
import com.manthau.postservice.shared.exception.NotFoundException;
import com.manthau.postservice.shared.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCategoryHandler {

    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Transactional
    public CategoryDto create(CategoryRequest req) {
        String name = normalizeName(req.name());
        String slug = normalizeSlug(req.slug(), name);
        ensureUniqueName(name);
        ensureUniqueSlug(slug);

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(normalizeDescription(req.description()))
                .build();
        return CategoryDto.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto update(String slug, CategoryRequest req) {
        Category category = findBySlug(slug);
        String name = normalizeName(req.name());
        if (!category.getName().equalsIgnoreCase(name)) {
            ensureUniqueName(name);
        }

        category.setName(name);
        category.setDescription(normalizeDescription(req.description()));
        return CategoryDto.from(categoryRepository.save(category));
    }

    @Transactional
    public void delete(String slug) {
        Category category = findBySlug(slug);
        if (postRepository.existsByCategorySlug(category.getSlug())) {
            throw new ConflictException("Category is used by posts");
        }
        categoryRepository.delete(category);
    }

    private Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private void ensureUniqueName(String name) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category name already exists");
        }
    }

    private void ensureUniqueSlug(String slug) {
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category slug already exists");
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeSlug(String slug, String fallbackName) {
        String source = slug == null || slug.isBlank() ? fallbackName : slug;
        String normalized = SlugUtils.slugify(source);
        if (normalized.isBlank()) {
            throw new BadRequestException("Category slug must not be blank");
        }
        return normalized;
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
