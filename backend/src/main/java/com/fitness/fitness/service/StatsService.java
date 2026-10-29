package com.fitness.fitness.service;

import com.fitness.fitness.vo.TrendVO;

public interface StatsService {
    TrendVO trend(Long userId, Integer days);
}
