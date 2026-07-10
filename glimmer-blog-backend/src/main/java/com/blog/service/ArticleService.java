package com.blog.service;

import com.blog.config.MarkdownUtil;
import com.blog.dto.*;
import com.blog.model.Article;
import com.blog.model.ArticleTag;
import com.blog.model.Category;
import com.blog.model.Tag;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MarkdownUtil markdownUtil;

    // 内存中的浏览量计数器，定时批量回写数据库
    private final ConcurrentHashMap<Long, Long> viewCounters = new ConcurrentHashMap<>();

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

    /**
     * 批量回写浏览量到数据库（由定时任务调用）
     */
    @Transactional
    public void flushViewCounts() {
        if (viewCounters.isEmpty()) return;

        Map<Long, Long> snapshot = new HashMap<>(viewCounters);
        viewCounters.clear();

        for (Map.Entry<Long, Long> entry : snapshot.entrySet()) {
            articleRepository.findById(entry.getKey()).ifPresent(article -> {
                article.setViews(article.getViews() + entry.getValue().intValue());
                articleRepository.save(article);
            });
        }
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
        // 使用内存计数器，避免每次访问都写库
        viewCounters.merge(id, 1L, Long::sum);
        article.setViews(article.getViews() + viewCounters.getOrDefault(id, 0L).intValue());
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
        List<Article> articles = page.getContent();
        if (articles.isEmpty()) {
            return new PageResponse<>(page.getTotalElements(), page.getTotalPages(), Collections.emptyList());
        }

        // 批量加载分类（1 次查询代替 N 次）
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Collections.emptyMap() :
                categoryRepository.findAllById(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));

        // 批量加载文章-标签关联（1 次查询代替 N 次）
        List<Long> articleIds = articles.stream().map(Article::getId).collect(Collectors.toList());
        Map<Long, List<ArticleTag>> articleTagsMap = articleTagRepository.findByArticleIdIn(articleIds).stream()
                .collect(Collectors.groupingBy(ArticleTag::getArticleId));

        // 批量加载标签（1 次查询代替 N 次）
        Set<Long> allTagIds = articleTagsMap.values().stream()
                .flatMap(List::stream)
                .map(ArticleTag::getTagId)
                .collect(Collectors.toSet());
        Map<Long, Tag> tagMap = allTagIds.isEmpty() ? Collections.emptyMap() :
                tagRepository.findAllById(allTagIds).stream()
                        .collect(Collectors.toMap(Tag::getId, t -> t));

        // 组装结果
        List<ArticleListResponse> list = articles.stream()
                .map(article -> toListResponse(article, categoryMap, articleTagsMap, tagMap))
                .collect(Collectors.toList());
        return new PageResponse<>(page.getTotalElements(), page.getTotalPages(), list);
    }

    private ArticleListResponse toListResponse(Article article,
                                               Map<Long, Category> categoryMap,
                                               Map<Long, List<ArticleTag>> articleTagsMap,
                                               Map<Long, Tag> tagMap) {
        ArticleListResponse resp = new ArticleListResponse();
        resp.setId(article.getId());
        resp.setTitle(article.getTitle());
        resp.setSummary(article.getSummary());
        resp.setCoverUrl(article.getCoverUrl());
        resp.setViews(article.getViews());
        resp.setIsPublished(article.getIsPublished());
        resp.setCreatedAt(article.getCreatedAt());

        // 从缓存 Map 中获取分类
        if (article.getCategoryId() != null && categoryMap.containsKey(article.getCategoryId())) {
            Category cat = categoryMap.get(article.getCategoryId());
            CategoryDTO dto = new CategoryDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            resp.setCategory(dto);
        }

        // 从缓存 Map 中获取标签
        List<ArticleTag> articleTags = articleTagsMap.getOrDefault(article.getId(), Collections.emptyList());
        if (!articleTags.isEmpty()) {
            List<TagDTO> tags = articleTags.stream()
                    .map(at -> tagMap.get(at.getTagId()))
                    .filter(Objects::nonNull)
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
        ArticleDetailResponse detail = new ArticleDetailResponse();
        detail.setId(article.getId());
        detail.setTitle(article.getTitle());
        detail.setContent(article.getContent());
        detail.setSummary(article.getSummary());
        detail.setCoverUrl(article.getCoverUrl());
        detail.setViews(article.getViews());
        detail.setIsPublished(article.getIsPublished());
        detail.setCreatedAt(article.getCreatedAt());
        detail.setHtmlContent(article.getHtmlCache());
        detail.setUpdatedAt(article.getUpdatedAt());

        // 加载分类
        if (article.getCategoryId() != null) {
            categoryRepository.findById(article.getCategoryId()).ifPresent(cat -> {
                CategoryDTO dto = new CategoryDTO();
                dto.setId(cat.getId());
                dto.setName(cat.getName());
                detail.setCategory(dto);
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
            detail.setTags(tags);
        } else {
            detail.setTags(Collections.emptyList());
        }

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
