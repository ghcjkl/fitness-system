package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("community_comment")
public class CommunityComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 所属动态ID
    private Long dynamicId;
    // 评论用户ID
    private Long userId;
    // 评论内容
    private String content;
    private LocalDateTime createTime;
}
