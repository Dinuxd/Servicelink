package com.servicelink.controller;

import com.servicelink.model.ServiceCategory;
import com.servicelink.repository.ServiceCategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ServiceCategoryRepository categories;

    public CategoryController(ServiceCategoryRepository categories) {
        this.categories = categories;
    }

    @Operation(summary = "List all service categories (public)")
    @GetMapping
    public List<ServiceCategory> all() {
        return categories.findAll();
    }
}
