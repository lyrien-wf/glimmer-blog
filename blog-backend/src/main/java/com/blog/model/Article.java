package com.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "html_cache", columnDefinition = "LONGTEXT")
    private String htmlCache;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "is_published", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isPublished = false;

    @Column(nullable = false)
    private Integer views = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private Category categoryObj;

    @Transient
    private java.util.List<Tag> tags;
}
