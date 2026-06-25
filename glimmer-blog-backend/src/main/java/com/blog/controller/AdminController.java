package com.blog.controller;

import com.blog.config.BlogConfig;
import com.blog.dto.*;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.TagService;
import com.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final UserService userService;
    private final BlogConfig blogConfig;

    @Autowired
    public AdminController(ArticleService articleService,
                           CategoryService categoryService,
                           TagService tagService,
                           UserService userService,
                           BlogConfig blogConfig) {
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.userService = userService;
        this.blogConfig = blogConfig;
    }

    // ===== 文章管理 =====

    @GetMapping("/articles")
    public ApiResponse<PageResponse<ArticleListResponse>> getArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(articleService.getAdminArticles(page, size));
    }

    @PostMapping("/articles")
    public ApiResponse<Map<String, Long>> createArticle(@RequestBody ArticleCreateRequest request) {
        Long id = articleService.createArticle(request);
        return ApiResponse.ok(Map.of("id", id));
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<Void> updateArticle(@PathVariable Long id,
                                           @RequestBody ArticleCreateRequest request) {
        articleService.updateArticle(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ApiResponse.ok();
    }

    @PostMapping("/articles/upload-md")
    public ApiResponse<Map<String, String>> uploadMd(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            return ApiResponse.ok(articleService.uploadMd(filename, content));
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败");
        }
    }

    // ===== 图片上传 =====

    private static final java.util.Set<String> ALLOWED_IMG_EXT =
            java.util.Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @PostMapping("/upload/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                    : "";
            if (!ALLOWED_IMG_EXT.contains(ext)) {
                throw new RuntimeException("不支持的图片格式，仅允许 jpg/png/gif/webp");
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;

            Path uploadPath = Paths.get(blogConfig.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            File dest = uploadPath.resolve(newFilename).toFile();
            file.transferTo(dest);

            return ApiResponse.ok(Map.of("url", "/uploads/" + newFilename));
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败");
        }
    }

    // ===== 分类管理 =====

    @PostMapping("/categories")
    public ApiResponse<Void> createCategory(@RequestBody Map<String, String> body) {
        categoryService.createCategory(body.get("name"));
        return ApiResponse.ok();
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok();
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        categoryService.updateCategory(id, body.get("name"));
        return ApiResponse.ok();
    }

    @PutMapping("/categories/sort")
    public ApiResponse<Void> sortCategories(@RequestBody java.util.Map<String, java.util.List<Long>> body) {
        categoryService.updateSortOrder(body.get("sortedIds"));
        return ApiResponse.ok();
    }

    // ===== 标签管理 =====

    @PostMapping("/tags")
    public ApiResponse<Void> createTag(@RequestBody Map<String, String> body) {
        tagService.createTag(body.get("name"));
        return ApiResponse.ok();
    }

    @DeleteMapping("/tags/{id}")
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.ok();
    }

    // ===== 密码修改 =====

    @PutMapping("/user/password")
    public ApiResponse<Void> changePassword(@RequestBody PasswordRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userService.changePassword(userId, request);
        return ApiResponse.ok();
    }
}
