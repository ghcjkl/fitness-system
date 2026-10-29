package com.fitness.fitness.vo;

import lombok.Data;

@Data
public class CalorieCalcVO {
    private String activity;
    private Double met;
    private Integer minutes;
    private Double weight;
    private Double calorie;
    // 计算说明（体重来源、是否估算）
    private String note;
}
