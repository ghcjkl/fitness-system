package com.fitness.fitness.service;

import com.fitness.fitness.dto.DietDTO;
import com.fitness.fitness.entity.Diet;
import com.fitness.fitness.vo.DailyNutritionVO;
import com.fitness.fitness.vo.NutritionGoalVO;
import com.fitness.fitness.vo.RecipeRecommendVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface DietService {
    void addDiet(DietDTO dto, Long userId);
    Page<Diet> pageList(Long pageNum, Long pageSize, Long userId);
    void removeByIdAndUserId(Long id, Long userId);
    DailyNutritionVO getDailyStats(LocalDate date, Long userId);
    /** 查询某月的全部饮食记录（导出用） */
    List<Diet> listByMonth(YearMonth month, Long userId);
    /** 每日营养目标与摄入对比 */
    NutritionGoalVO getDailyGoal(LocalDate date, Long userId);
    /** 按剩余营养目标推荐一餐搭配 */
    RecipeRecommendVO recommend(LocalDate date, Long userId, String mealType, Integer variant);
}