package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_history")
public class HealthHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    // 体重kg
    private Double weight;
    // 体脂率(%)
    private Double bodyFat;
    // 记录日期
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
