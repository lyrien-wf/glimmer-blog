package com.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailResponse extends ArticleListResponse {
    private String content;       // Markdown 原文（用于编辑器回填）
    private String htmlContent;   // 渲染后的 HTML
    private LocalDateTime updatedAt;
}
