package com.fitness.fitness.vo;

import lombok.Data;
import java.util.List;

@Data
public class TrendVO {
    // 日期序列 yyyy-MM-dd
    private List<String> dates;
    // 体重kg，无记录为null
    private List<Double> weight;
    // 体脂率%，无记录为null
    private List<Double> bodyFat;
    // 每日运动时长（分钟）
    private List<Integer> sportDuration;
    // 每日饮食摄入热量（kcal）
    private List<Double> calorie;
}
