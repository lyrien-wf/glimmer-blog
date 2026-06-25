package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.dto.ArticleDetailResponse;
import com.blog.dto.ArticleListResponse;
import com.blog.dto.PageResponse;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @Autowired
    public ArticleController(ArticleService articleService,
                             CategoryService categoryService,
                             TagService tagService) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    @GetMapping("/articles")
    public ApiResponse<PageResponse<ArticleListResponse>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        return ApiResponse.ok(articleService.getPublishedArticles(page, size, categoryId, tagId));
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<ArticleDetailResponse> getArticle(@PathVariable Long id) {
        return ApiResponse.ok(articleService.getArticleDetail(id));
    }

    @GetMapping("/articles/search")
    public ApiResponse<PageResponse<ArticleListResponse>> searchArticles(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(articleService.searchArticles(q, page, size));
    }

    @GetMapping("/categories")
    public ApiResponse<?> getCategories() {
        return ApiResponse.ok(categoryService.getAllCategories());
    }

    @GetMapping("/tags")
    public ApiResponse<?> getTags() {
        return ApiResponse.ok(tagService.getAllTags());
    }
}
