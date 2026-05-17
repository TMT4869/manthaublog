package com.manthau.postservice.category.list;

import com.manthau.postservice.category.domain.CategoryRepository;
import com.manthau.postservice.category.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCategoriesHandler {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> handle() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryDto::from)
                .toList();
    }
}
