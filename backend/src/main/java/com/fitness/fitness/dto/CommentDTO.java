package com.fitness.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 300, message = "评论最多 300 字")
    private String content;
}
