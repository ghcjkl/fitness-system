package com.fitness.fitness.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运动打卡连续天数与提醒
 */
@Data
public class SportStreakVO {
    /** 当前连续打卡天数（今天未打卡则统计到昨天） */
    private Integer currentStreak;
    /** 历史最长连续打卡天数 */
    private Integer bestStreak;
    /** 累计打卡天数 */
    private Integer totalDays;
    /** 今天是否已打卡 */
    private Boolean todayDone;
    /** 最近 7 天已打卡天数 */
    private Integer weekDone;
    /** 最近 7 天打卡情况 */
    private List<DayItem> week;
    /** 运动提醒文案 */
    private String reminder;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayItem {
        /** yyyy-MM-dd */
        private String date;
        /** 展示标签：日期数字或“今天” */
        private String label;
        private Boolean done;
        private Boolean today;
    }
}
