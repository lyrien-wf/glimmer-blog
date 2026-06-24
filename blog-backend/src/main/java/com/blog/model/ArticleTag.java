package com.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "article_tag")
@IdClass(ArticleTagId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTag {

    @Id
    @Column(name = "article_id")
    private Long articleId;

    @Id
    @Column(name = "tag_id")
    private Long tagId;
}
