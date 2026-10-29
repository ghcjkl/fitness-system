package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_plan")
public class AiPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    // 方案类型：DIET饮食 / SPORT运动
    private String planType;
    // 方案内容
    private String content;
    // RAG溯源引用（JSON字符串）
    private String sources;
    private LocalDateTime createTime;
}
