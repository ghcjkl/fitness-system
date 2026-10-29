package com.fitness.fitness.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NutritionGoalVO {
    private Double targetCalorie = 0.0;
    private Double targetProtein = 0.0;
    private Double targetCarb = 0.0;
    private Double targetFat = 0.0;

    private Double intakeCalorie = 0.0;
    private Double intakeProtein = 0.0;
    private Double intakeCarb = 0.0;
    private Double intakeFat = 0.0;

    private Integer caloriePercent = 0;
    private Integer proteinPercent = 0;
    private Integer carbPercent = 0;
    private Integer fatPercent = 0;

    // 超标/不足提醒
    private List<String> warnings = new ArrayList<>();
    // 目标计算依据说明
    private String basis;
}
