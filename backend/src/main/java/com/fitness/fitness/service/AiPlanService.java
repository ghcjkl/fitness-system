package com.fitness.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.AiPlan;
import java.util.Map;

public interface AiPlanService {
    Map<String, Object> generate(String type, String remark, Long userId);
    AiPlan latest(String type, Long userId);
    Page<AiPlan> history(String type, Long pageNum, Long pageSize, Long userId);
}
