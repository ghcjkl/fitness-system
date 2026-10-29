package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("food")
public class Food {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 食物名称
    private String name;
    // 别名，搜索用
    private String alias;
    // 分类：谷薯/肉蛋/蔬菜/水果/乳制品/豆制品/饮品零食
    private String category;
    // 每100g热量(kcal)
    private Double calorie;
    // 每100g蛋白质(g)
    private Double protein;
    // 每100g碳水(g)
    private Double carb;
    // 每100g脂肪(g)
    private Double fat;
    // 膳食纤维(g)
    private Double fiber;
    // 钠(mg)
    private Double sodium;
    // 常见份量，如 1个约50g
    private String standardPortion;
    // 简介/食用建议
    private String description;
    private LocalDateTime createTime;
}
