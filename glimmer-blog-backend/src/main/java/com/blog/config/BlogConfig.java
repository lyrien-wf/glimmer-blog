package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogConfig {
    private String jwtSecret;
    private Integer jwtExpireHours;
    private String uploadDir;
}
