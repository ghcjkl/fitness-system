package com.fitness.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.vo.CommentVO;
import com.fitness.fitness.vo.CommunityVO;

public interface CommunityService {
    void publish(String content, String imgUrl, Long userId);
    Page<CommunityVO> pageAll(Long pageNum, Long pageSize);
    void addLike(Long id);
    void deleteMyDynamic(Long id, Long userId);
    /** 发表评论 */
    void addComment(Long dynamicId, String content, Long userId);
    /** 分页查询某条动态的评论 */
    Page<CommentVO> pageComments(Long dynamicId, Long pageNum, Long pageSize);
    /** 删除本人评论 */
    void deleteMyComment(Long id, Long userId);
}
