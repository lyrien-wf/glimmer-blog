package com.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleListResponse {
    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private CategoryDTO category;
    private List<TagDTO> tags;
    private Boolean isPublished;
    private Integer views;
    private LocalDateTime createdAt;
}
