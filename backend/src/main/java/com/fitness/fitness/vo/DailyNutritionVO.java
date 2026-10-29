package com.fitness.fitness.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DailyNutritionVO {
    private Double calorie = 0.0;
    private Double protein = 0.0;
    private Double carb = 0.0;
    private Double fat = 0.0;
    private List<String> warnings = new ArrayList<>();
    private boolean hasData = false;
}