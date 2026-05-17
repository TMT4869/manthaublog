package com.manthau.postservice.category.admin;

import com.manthau.postservice.category.dto.CategoryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryHandler handler;

    @PostMapping("/api/admin/categories")
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(handler.create(req));
    }

    @PutMapping("/api/admin/categories/{slug}")
    public CategoryDto update(@PathVariable String slug, @Valid @RequestBody CategoryRequest req) {
        return handler.update(slug, req);
    }

    @DeleteMapping("/api/admin/categories/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        handler.delete(slug);
        return ResponseEntity.noContent().build();
    }
}
