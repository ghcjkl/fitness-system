package com.fitness.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.dto.PlanRequestDTO;
import com.fitness.fitness.entity.AiPlan;
import com.fitness.fitness.service.AiPlanService;
import com.fitness.fitness.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai/plan")
public class AiPlanController {

    @Autowired
    private AiPlanService aiPlanService;

    /**
     * 生成个性化方案，type 支持 diet / sport
     */
    @PostMapping("/{type}")
    public Result<Map<String, Object>> generate(@PathVariable String type,
                                                @RequestBody(required = false) PlanRequestDTO dto,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        String remark = dto == null ? null : dto.getRemark();
        return Result.success(aiPlanService.generate(type, remark, userId), "方案生成完成");
    }

    /**
     * 查询最近一次方案
     */
    @GetMapping("/latest")
    public Result<AiPlan> latest(@RequestParam String type, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(aiPlanService.latest(type, userId));
    }

    /**
     * 查询方案历史
     */
    @GetMapping("/history")
    public Result<Page<AiPlan>> history(@RequestParam String type,
                                        @RequestParam(defaultValue = "1") Long pageNum,
                                        @RequestParam(defaultValue = "10") Long pageSize,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(aiPlanService.history(type, pageNum, pageSize, userId));
    }
}
