package com.fitness.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatDTO {
    @NotBlank(message = "提问内容不能为空")
    private String question;
}