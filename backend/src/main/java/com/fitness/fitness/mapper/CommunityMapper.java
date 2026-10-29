package com.fitness.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.fitness.entity.Community;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface CommunityMapper extends BaseMapper<Community> {
    /** 动态点赞，点赞数+1 */
    @Update("UPDATE community SET like_count = like_count + 1 WHERE id = #{id}")
    void addLike(@Param("id") Long id);
}