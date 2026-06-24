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

    Page<Article> findByIsPublishedTrueAndTitleContainingOrIsPublishedTrueAndContentContaining(
            String title, String content, Pageable pageable);

    Page<Article> findByIsPublishedOrderByCreatedAtDesc(Boolean isPublished, Pageable pageable);

    long countByCategoryId(Long categoryId);
}
