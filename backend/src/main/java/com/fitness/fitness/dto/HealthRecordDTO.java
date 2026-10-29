package com.fitness.fitness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HealthRecordDTO {
    // 用户ID自动从token获取，前端不传
    @NotNull(message = "年龄不能为空")
    private Integer age;

    @NotNull(message = "身高不能为空")
    private Double height;

    @NotNull(message = "体重不能为空")
    private Double weight;

    // 体脂率(%)
    private Double bodyFat;

    // 1男 2女
    private Integer gender;

    // 基础疾病：高血压/糖尿病等
    private String disease;

    // 运动目标：减脂/增肌/塑形
    private String target;

    // 饮食禁忌
    private String dietAvoid;

    // 过敏食材
    private String allergy;

    // 运动旧伤
    private String oldInjury;
}