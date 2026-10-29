package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("diet")
public class Diet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    // 餐次：早餐/午餐/晚餐/加餐
    private String mealType;
    // 食物名称
    private String foodName;
    // 食用量
    private Double intake;
    // 热量
    private Double calorie;
    // 蛋白质(克)
    private Double protein;
    // 碳水化合物(克)
    private Double carb;
    // 脂肪(克)
    private Double fat;
    // 餐食照片地址
    private String imageUrl;
    // 打卡日期
    private LocalDate recordDate;
    private LocalDateTime createTime;
}