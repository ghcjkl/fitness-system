package com.fitness.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DietDTO {
    @NotBlank(message = "餐次不能为空")
    private String mealType; // 早餐/午餐/晚餐/加餐

    @NotBlank(message = "食物名称不能为空")
    private String foodName;

    @NotNull(message = "摄入量不能为空")
    private Double intake;

    @NotNull(message = "热量不能为空")
    private Double calorie;

    // 蛋白质(克)
    private Double protein;
    // 碳水化合物(克)
    private Double carb;
    // 脂肪(克)
    private Double fat;
    // 餐食照片地址
    private String imageUrl;

    private LocalDate recordDate;
}