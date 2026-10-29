package com.fitness.fitness.controller;

import com.fitness.fitness.dto.HealthRecordDTO;
import com.fitness.fitness.entity.HealthRecord;
import com.fitness.fitness.service.HealthRecordService;
import com.fitness.fitness.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    /**
     * 新增/更新健康档案
     */
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody HealthRecordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        healthRecordService.saveRecord(dto, userId);
        return Result.success(null, "档案保存成功");
    }

    /**
     * 查询本人健康档案
     */
    @GetMapping("/get")
    public Result<HealthRecord> get(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        HealthRecord record = healthRecordService.getByUserId(userId);
        return Result.success(record);
    }
}