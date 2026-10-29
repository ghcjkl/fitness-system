package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sport")
public class Sport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    // 运动类型：跑步/力量/瑜伽
    private String sportType;
    // 训练动作
    private String exerciseName;
    // 组数
    private Integer sets;
    // 负重(kg)
    private Double weight;
    // 运动时长(分钟)
    private Integer duration;
    // 消耗热量
    private Double consumeCalorie;
    // 身体酸痛部位
    private String soreArea;
    // 备注
    private String remark;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}