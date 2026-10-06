package com.blog.repository;

import com.blog.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Article> findByCategoryIdAndIsPublishedTrueOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @Query(value = "SELECT a.* FROM article a JOIN article_tag at ON a.id = at.article_id WHERE at.tag_id = :tagId AND a.is_published = 1 ORDER BY a.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM article a JOIN article_tag at ON a.id = at.article_id WHERE at.tag_id = :tagId AND a.is_published = 1",
           nativeQuery = true)
    Page<Article> findByTagIdAndIsPublishedTrue(@Param("tagId") Long tagId, Pageable pageable);

    /**
     * 关键词搜索。pattern 由调用方构造（已转义 % _ \ 并加通配符），
     * 因此这里用普通参数占位而不是 Spring Data 的 %:q% 语法。
     * 必须带 ORDER BY：否则分页顺序不稳定，翻页可能重复或漏条。
     */
    @Query("SELECT a FROM Article a WHERE a.isPublished = true " +
           "AND (a.title LIKE :pattern OR a.content LIKE :pattern) " +
           "ORDER BY a.createdAt DESC")
    Page<Article> searchPublished(@Param("pattern") String pattern, Pageable pageable);

    @Query(value = "SELECT * FROM article ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM article",
           nativeQuery = true)
    Page<Article> findAllOrderByCreatedAtDesc(Pageable pageable);

    long countByCategoryId(Long categoryId);
}
