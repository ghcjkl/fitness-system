package com.fitness.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.fitness.entity.HealthRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
    /** 根据用户ID查询健康档案 */
    @Select("SELECT * FROM health_record WHERE user_id = #{userId} LIMIT 1")
    HealthRecord selectByUserId(@Param("userId") Long userId);
}