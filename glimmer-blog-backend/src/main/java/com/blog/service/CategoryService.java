package com.blog.service;

import com.blog.dto.CategoryDTO;
import com.blog.model.Article;
import com.blog.model.Category;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(cat -> cat.getSortOrder() != null ? cat.getSortOrder() : 0))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO createCategory(String name) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new RuntimeException("分类已存在");
        }
        Category category = new Category();
        category.setName(name);
        return toDTO(categoryRepository.save(category));
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        long count = articleRepository.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("该分类下还有 " + count + " 篇文章，无法删除");
        }
        categoryRepository.delete(category);
    }

    public void updateCategory(Long id, String name) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        categoryRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("分类名已存在");
            }
        });
        category.setName(name);
        categoryRepository.save(category);
    }

    public void updateSortOrder(java.util.List<Long> sortedIds) {
        for (int i = 0; i < sortedIds.size(); i++) {
            categoryRepository.findById(sortedIds.get(i)).ifPresent(cat -> {
                cat.setSortOrder(sortedIds.indexOf(sortedIds.get(i)));
                categoryRepository.save(cat);
            });
        }
        // 按新顺序设置 sortOrder
        for (int i = 0; i < sortedIds.size(); i++) {
            final int order = i;
            categoryRepository.findById(sortedIds.get(i)).ifPresent(cat -> {
                cat.setSortOrder(order);
                categoryRepository.save(cat);
            });
        }
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSortOrder(category.getSortOrder());
        return dto;
    }
}
