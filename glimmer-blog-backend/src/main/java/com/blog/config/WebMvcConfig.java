package com.blog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final BlogConfig blogConfig;

    @Autowired
    public WebMvcConfig(BlogConfig blogConfig) {
        this.blogConfig = blogConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(blogConfig.getUploadDir());
        // 必须先确保目录存在：下面 toUri() 的末尾斜杠依赖"目录已存在"这一判断
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录：" + uploadPath, e);
        }
        // Path.toUri() 仅在路径已是目录时才补末尾斜杠。
        // 若启动时 uploads 还不存在，得到的是不带斜杠的 file:/.../uploads，
        // 会导致 /uploads/** 无法解析相对文件名（表现为上传后立刻 404/500，重启后才正常）。
        String location = uploadPath.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
