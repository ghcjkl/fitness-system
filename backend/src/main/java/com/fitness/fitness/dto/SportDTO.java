package com.fitness.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SportDTO {
    @NotBlank(message = "运动类型不能为空")
    private String sportType; // 跑步、力量、瑜伽

    // 训练动作
    private String exerciseName;
    // 组数
    private Integer sets;
    // 负重(kg)
    private Double weight;
    // 训练动作时长(分钟)
    @NotNull(message = "运动时长不能为空")
    private Integer duration; // 分钟

    private Double consumeCalorie; // 消耗热量

    // 身体酸痛部位
    private String soreArea;

    private String remark;

    private LocalDate recordDate;
}