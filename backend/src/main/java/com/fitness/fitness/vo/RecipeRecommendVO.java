package com.fitness.fitness.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RecipeRecommendVO {
    private String mealType;
    // 本餐目标（按今日剩余目标折算）
    private Double targetCalorie = 0.0;
    private Double targetProtein = 0.0;
    private Double targetCarb = 0.0;
    private Double targetFat = 0.0;

    // 推荐组合合计
    private Double totalCalorie = 0.0;
    private Double totalProtein = 0.0;
    private Double totalCarb = 0.0;
    private Double totalFat = 0.0;

    private List<RecipeItem> items = new ArrayList<>();
    private String tip;

    @Data
    public static class RecipeItem {
        private Long foodId;
        private String name;
        private String category;
        private Double grams;
        private Double calorie;
        private Double protein;
        private Double carb;
        private Double fat;
    }
}
