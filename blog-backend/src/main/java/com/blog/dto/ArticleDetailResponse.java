package com.blog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailResponse extends ArticleListResponse {
    private String htmlContent;
    private LocalDateTime updatedAt;
}
