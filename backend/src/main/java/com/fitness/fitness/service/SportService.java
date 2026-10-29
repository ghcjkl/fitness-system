package com.fitness.fitness.service;

import com.fitness.fitness.dto.SportDTO;
import com.fitness.fitness.entity.Sport;
import com.fitness.fitness.vo.CalorieCalcVO;
import com.fitness.fitness.vo.MetVO;
import com.fitness.fitness.vo.SportStreakVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.YearMonth;
import java.util.List;

public interface SportService {
    void addSport(SportDTO dto, Long userId);
    Page<Sport> pageList(Long pageNum, Long pageSize, Long userId);
    void removeByIdAndUserId(Long id, Long userId);
    /** 查询某月的全部运动记录（导出用） */
    List<Sport> listByMonth(YearMonth month, Long userId);
    /** 支持的运动项目及 MET 强度 */
    List<MetVO> metList();
    /** 按 MET 公式计算消耗热量 */
    CalorieCalcVO calcCalorie(String activity, Integer minutes, Long userId);
    /** 连续打卡天数与运动提醒 */
    SportStreakVO streak(Long userId);
}