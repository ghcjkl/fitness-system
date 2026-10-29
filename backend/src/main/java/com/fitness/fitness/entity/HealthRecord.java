package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 所属用户id
    private Long userId;
    // 年龄
    private Integer age;
    // 身高
    private Double height;
    // 体重
    private Double weight;
    // 体脂率(%)
    private Double bodyFat;
    // 性别
    private Integer gender;
    // 基础疾病
    private String disease;
    // 健身目标：减脂/增肌/塑形
    private String target;
    // 饮食禁忌
    private String dietAvoid;
    // 过敏食材
    private String allergy;
    // 运动旧伤
    private String oldInjury;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}