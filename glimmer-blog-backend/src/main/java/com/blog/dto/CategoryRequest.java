package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "分类名不能为空")
    @Size(max = 50, message = "分类名不能超过 50 字")
    private String name;
}
