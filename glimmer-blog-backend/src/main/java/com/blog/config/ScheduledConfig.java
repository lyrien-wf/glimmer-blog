package com.blog.config;

import com.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledConfig {

    private final ArticleService articleService;

    @Autowired
    public ScheduledConfig(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 每 60 秒将内存中的浏览量批量写回数据库
     */
    @Scheduled(fixedRate = 60000)
    public void flushViewCounts() {
        articleService.flushViewCounts();
    }
}
