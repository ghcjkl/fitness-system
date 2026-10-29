package com.fitness.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Food;

import java.util.List;

public interface FoodService {
    /** 按名称/别名关键字与分类分页查询食材 */
    Page<Food> search(String keyword, String category, Long pageNum, Long pageSize);

    /** 查询全部食材分类 */
    List<String> listCategories();

    /** 查询食材详情 */
    Food getDetail(Long id);
}
