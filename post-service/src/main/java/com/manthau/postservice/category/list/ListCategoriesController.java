package com.manthau.postservice.category.list;

import com.manthau.postservice.category.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ListCategoriesController {

    private final ListCategoriesHandler handler;

    @GetMapping("/api/categories")
    public List<CategoryDto> listCategories() {
        return handler.handle();
    }
}
