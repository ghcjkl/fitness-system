package com.fitness.fitness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.fitness.entity.Food;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FoodMapper extends BaseMapper<Food> {

    /** 查询全部分类（用于前端筛选） */
    @Select("SELECT DISTINCT category FROM food ORDER BY category")
    List<String> selectCategories();
}
