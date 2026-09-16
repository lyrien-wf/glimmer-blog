package com.blog.config;

import lombok.Data;
import java.nio.file.Paths;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogConfig {
    private String jwtSecret;
    private Integer jwtExpireHours;
    private String uploadDir;
    /**
     * 上传目录必须在此处就解析为绝对路径，作为全项目唯一基准。
     * 不能留相对路径：Servlet 的 Part.write / MultipartFile.transferTo 对相对路径
     * 是以 multipart 临时目录为基准解析的，而 Files/Paths 是以进程工作目录为基准，
     * 保持相对路径会让「建目录」「写文件」「静态资源读取」三处指向不同位置，
     * 结果是上传必定失败（目标父目录不存在），或文件落到重启即失的临时目录。
     */
    public void setUploadDir(String uploadDir) {
        this.uploadDir = (uploadDir == null || uploadDir.isBlank())
                ? null
                : Paths.get(uploadDir).toAbsolutePath().normalize().toString();
    }
}
