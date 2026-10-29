package com.fitness.fitness.controller;

import com.fitness.fitness.entity.MedicalReport;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.service.ReportService;
import com.fitness.fitness.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 上传体检报告（可选填体检日期）
     */
    @PostMapping("/upload")
    public Result<MedicalReport> upload(@RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String reportDate,
                                        HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("loginUserId");
        LocalDate date = null;
        if (reportDate != null && !reportDate.isBlank()) {
            try {
                date = LocalDate.parse(reportDate);
            } catch (Exception e) {
                throw new BusinessException("体检日期格式应为 yyyy-MM-dd");
            }
        }
        MedicalReport report = reportService.upload(file, date, userId);
        return Result.success(report, "报告上传成功");
    }

    /**
     * 查询本人全部体检报告
     */
    @GetMapping("/list")
    public Result<List<MedicalReport>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(reportService.list(userId));
    }

    /**
     * 查询报告详情
     */
    @GetMapping("/{id}")
    public Result<MedicalReport> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(reportService.getDetail(id, userId));
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        reportService.delete(id, userId);
        return Result.success(null, "报告已删除");
    }
}
