package com.fitness.fitness.serviceimpl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.AiPlan;
import com.fitness.fitness.entity.HealthRecord;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.AiPlanMapper;
import com.fitness.fitness.service.AiPlanService;
import com.fitness.fitness.service.HealthRecordService;
import com.fitness.fitness.util.AiHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiPlanServiceImpl implements AiPlanService {
    private static final String DIET = "DIET";
    private static final String SPORT = "SPORT";

    @Autowired
    private AiPlanMapper aiPlanMapper;
    @Autowired
    private AiHttpUtil aiHttpUtil;
    @Autowired
    private HealthRecordService healthRecordService;

    @Override
    public Map<String, Object> generate(String type, String remark, Long userId) {
        String planType = normalize(type);
        HealthRecord record = healthRecordService.getByUserId(userId);
        if (record == null) {
            throw new BusinessException("请先填写健康档案，再生成个性化方案");
        }

        Map<String, Object> param = new HashMap<>();
        param.put("health", record);
        param.put("planType", planType);
        if (remark != null && !remark.isBlank()) {
            param.put("remark", remark);
        }

        String uri = DIET.equals(planType) ? "/rag/plan/diet" : "/rag/plan/sport";
        Map<String, Object> aiResult;
        try {
            aiResult = aiHttpUtil.postAi(uri, param);
        } catch (Exception e) {
            throw new BusinessException("AI 服务暂不可用，请先启动 Python 服务（127.0.0.1:8000）");
        }
        if (aiResult == null || aiResult.get("answer") == null) {
            throw new BusinessException("AI 服务返回结果异常");
        }

        AiPlan plan = new AiPlan();
        plan.setUserId(userId);
        plan.setPlanType(planType);
        plan.setContent(aiResult.get("answer").toString());
        Object sources = aiResult.get("sources");
        if (sources != null) {
            plan.setSources(JSONUtil.toJsonStr(sources));
        }
        plan.setCreateTime(LocalDateTime.now());
        aiPlanMapper.insert(plan);

        Map<String, Object> result = new HashMap<>(aiResult);
        result.put("planId", plan.getId());
        result.put("planType", planType);
        result.put("createTime", plan.getCreateTime().toString());
        return result;
    }

    @Override
    public AiPlan latest(String type, Long userId) {
        String planType = normalize(type);
        return aiPlanMapper.selectOne(Wrappers.<AiPlan>lambdaQuery()
                .eq(AiPlan::getUserId, userId)
                .eq(AiPlan::getPlanType, planType)
                .orderByDesc(AiPlan::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public Page<AiPlan> history(String type, Long pageNum, Long pageSize, Long userId) {
        String planType = normalize(type);
        Page<AiPlan> page = new Page<>(pageNum, pageSize);
        return aiPlanMapper.selectPage(page, Wrappers.<AiPlan>lambdaQuery()
                .eq(AiPlan::getUserId, userId)
                .eq(AiPlan::getPlanType, planType)
                .orderByDesc(AiPlan::getCreateTime));
    }

    private String normalize(String type) {
        if (type == null || type.isBlank()) {
            throw new BusinessException("方案类型不能为空");
        }
        String planType = type.trim().toUpperCase();
        if (!DIET.equals(planType) && !SPORT.equals(planType)) {
            throw new BusinessException("方案类型只支持 diet 或 sport");
        }
        return planType;
    }
}
