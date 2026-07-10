package com.blog.service;

import com.blog.config.MarkdownUtil;
import com.blog.dto.*;
import com.blog.model.Article;
import com.blog.model.ArticleTag;
import com.blog.model.Category;
import com.blog.repository.ArticleRepository;
import com.blog.repository.ArticleTagRepository;
import com.blog.repository.CategoryRepository;
import com.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MarkdownUtil markdownUtil;

    @Autowired
    public ArticleService(ArticleRepository articleRepository,
                          ArticleTagRepository articleTagRepository,
                          CategoryRepository categoryRepository,
                          TagRepository tagRepository,
                          MarkdownUtil markdownUtil) {
        this.articleRepository = articleRepository;
        this.articleTagRepository = articleTagRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.markdownUtil = markdownUtil;
    }

    // ===== 公开接口 =====

    public PageResponse<ArticleListResponse> getPublishedArticles(Integer page, Integer size, Long categoryId, Long tagId) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage;

        if (categoryId != null) {
            articlePage = articleRepository.findByCategoryIdAndIsPublishedTrueOrderByCreatedAtDesc(categoryId, pageable);
        } else if (tagId != null) {
            articlePage = articleRepository.findByTagIdAndIsPublishedTrue(tagId, pageable);
        } else {
            articlePage = articleRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable);
        }

        return toPageResponse(articlePage);
    }

    public ArticleDetailResponse getArticleDetail(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        if (!Boolean.TRUE.equals(article.getIsPublished())) {
            throw new RuntimeException("文章不存在");
        }
        article.setViews(article.getViews() + 1);
        articleRepository.save(article);
        return toDetailResponse(article);
    }

    public PageResponse<ArticleListResponse> searchArticles(String q, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.searchPublished(q, pageable);
        return toPageResponse(articlePage);
    }

    // ===== 管理接口 =====

    public PageResponse<ArticleListResponse> getAdminArticles(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> articlePage = articleRepository.findAllOrderByCreatedAtDesc(pageable);
        return toPageResponse(articlePage);
    }

    public ArticleDetailResponse getAdminArticleDetail(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        return toDetailResponse(article);
    }

    @Transactional
    public Long createArticle(ArticleCreateRequest req) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        article.setSummary(req.getSummary());
        article.setCoverUrl(req.getCoverUrl());
        article.setCategoryId(req.getCategoryId() != null ? req.getCategoryId() : getDefaultCategoryId());
        article.setIsPublished(req.getIsPublished() != null && req.getIsPublished());
        article.setHtmlCache(markdownUtil.renderToHtml(req.getContent()));

        // 自动摘要
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String plainText = article.getContent().replaceAll("#+ ", "").replaceAll("[*_`~>\\-|]", "").trim();
            article.setSummary(plainText.length() > 100 ? plainText.substring(0, 100) + "..." : plainText);
        }

        Article saved = articleRepository.save(article);
        saveArticleTags(saved.getId(), req.getTagIds());
        return saved.getId();
    }

    @Transactional
    public void updateArticle(Long id, ArticleCreateRequest req) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        article.setTitle(req.getTitle());
        article.setContent(req.getContent());
        article.setSummary(req.getSummary());
        article.setCoverUrl(req.getCoverUrl());
        article.setCategoryId(req.getCategoryId() != null ? req.getCategoryId() : getDefaultCategoryId());
        article.setIsPublished(req.getIsPublished() != null && req.getIsPublished());
        article.setHtmlCache(markdownUtil.renderToHtml(req.getContent()));

        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            String plainText = article.getContent().replaceAll("#+ ", "").replaceAll("[*_`~>\\-|]", "").trim();
            article.setSummary(plainText.length() > 100 ? plainText.substring(0, 100) + "..." : plainText);
        }

        articleRepository.save(article);
        articleTagRepository.deleteByArticleId(id);
        saveArticleTags(id, req.getTagIds());
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("文章不存在");
        }
        articleTagRepository.deleteByArticleId(id);
        articleRepository.deleteById(id);
    }

    public Map<String, String> uploadMd(String filename, String content) {
        Map<String, String> result = new HashMap<>();
        // 从文件名提取标题（去掉 .md 后缀）
        String title = filename.replaceAll("\\.md$", "").replaceAll("_", " ");
        result.put("title", title);
        result.put("content", content);
        return result;
    }

    // ===== 辅助方法 =====

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds) {
            ArticleTag at = new ArticleTag();
            at.setArticleId(articleId);
            at.setTagId(tagId);
            articleTagRepository.save(at);
        }
    }

    private PageResponse<ArticleListResponse> toPageResponse(Page<Article> page) {
        List<ArticleListResponse> list = page.getContent().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(page.getTotalElements(), page.getTotalPages(), list);
    }

    private ArticleListResponse toListResponse(Article article) {
        ArticleListResponse resp = new ArticleListResponse();
        resp.setId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSummary(article.getSummary());
        resp.setCoverUrl(article.getCoverUrl());
        resp.setViews(article.getViews());
        resp.setIsPublished(article.getIsPublished());
        resp.setCreatedAt(article.getCreatedAt());

        // 加载分类
        if (article.getCategoryId() != null) {
            categoryRepository.findById(article.getCategoryId()).ifPresent(cat -> {
                CategoryDTO dto = new CategoryDTO();
                dto.setId(cat.getId());
                dto.setName(cat.getName());
                resp.setCategory(dto);
            });
        }

        // 加载标签
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(article.getId());
        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream().map(ArticleTag::getTagId).collect(Collectors.toList());
            List<TagDTO> tags = tagRepository.findAllById(tagIds).stream()
                    .map(tag -> {
                        TagDTO dto = new TagDTO();
                        dto.setId(tag.getId());
                        dto.setName(tag.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());
            resp.setTags(tags);
        } else {
            resp.setTags(Collections.emptyList());
        }

        return resp;
    }

    private ArticleDetailResponse toDetailResponse(Article article) {
        ArticleListResponse listResp = toListResponse(article);
        ArticleDetailResponse detail = new ArticleDetailResponse();
        detail.setId(listResp.getId());
        detail.setTitle(listResp.getTitle());
        detail.setContent(article.getContent());  // 添加 Markdown 原文
        detail.setSummary(listResp.getSummary());
        detail.setCoverUrl(listResp.getCoverUrl());
        detail.setCategory(listResp.getCategory());
        detail.setTags(listResp.getTags());
        detail.setViews(listResp.getViews());
        detail.setIsPublished(listResp.getIsPublished());
        detail.setCreatedAt(listResp.getCreatedAt());
        detail.setHtmlContent(article.getHtmlCache());
        detail.setUpdatedAt(article.getUpdatedAt());
        return detail;
    }

    /**
     * 获取默认分类 ID（"其他"分类）
     */
    private Long getDefaultCategoryId() {
        return categoryRepository.findByName("其他")
                .map(Category::getId)
                .orElse(null);
    }
}
