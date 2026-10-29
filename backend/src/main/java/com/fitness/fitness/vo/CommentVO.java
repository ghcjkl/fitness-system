package com.fitness.fitness.vo;

import com.fitness.fitness.entity.CommunityComment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends CommunityComment {
    // 评论者昵称
    private String nickname;
    // 评论者头像
    private String avatar;
}
