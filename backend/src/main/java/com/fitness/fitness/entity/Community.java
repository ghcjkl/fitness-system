package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("community")
public class Community {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    // 动态内容
    private String content;
    // 图片路径，逗号分隔多张
    private String imgUrl;
    // 点赞数
    private Integer likeCount;
    private LocalDateTime createTime;
}