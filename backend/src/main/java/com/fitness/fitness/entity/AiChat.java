package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_chat")
public class AiChat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    // 用户提问
    private String question;
    // AI返回方案
    private String answer;
    // RAG溯源引用（JSON字符串）
    private String sources;
    private LocalDateTime createTime;
}