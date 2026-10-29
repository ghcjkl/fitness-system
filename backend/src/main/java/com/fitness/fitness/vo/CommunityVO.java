package com.fitness.fitness.vo;

import com.fitness.fitness.entity.Community;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityVO extends Community {
    // 发布者昵称
    private String nickname;
    // 发布者头像
    private String avatar;
    // 评论数
    private Integer commentCount;
}
