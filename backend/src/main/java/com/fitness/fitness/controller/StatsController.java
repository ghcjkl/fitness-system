package com.fitness.fitness.controller;

import com.fitness.fitness.service.StatsService;
import com.fitness.fitness.vo.Result;
import com.fitness.fitness.vo.TrendVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 健康数据趋势：体重、体脂率、每日运动时长、每日饮食热量
     */
    @GetMapping("/trend")
    public Result<TrendVO> trend(@RequestParam(defaultValue = "30") Integer days,
                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(statsService.trend(userId, days));
    }
}
