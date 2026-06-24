package com.blog.repository;

import com.blog.model.ArticleTag;
import com.blog.model.ArticleTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId> {
    List<ArticleTag> findByArticleId(Long articleId);
    void deleteByArticleId(Long articleId);
}
