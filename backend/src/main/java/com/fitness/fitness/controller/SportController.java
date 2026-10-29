package com.fitness.fitness.controller;

import com.fitness.fitness.dto.SportDTO;
import com.fitness.fitness.entity.Sport;
import com.fitness.fitness.service.SportService;
import com.fitness.fitness.util.ExcelExportUtil;
import com.fitness.fitness.vo.CalorieCalcVO;
import com.fitness.fitness.vo.MetVO;
import com.fitness.fitness.vo.Result;
import com.fitness.fitness.vo.SportStreakVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/sport")
public class SportController {

    @Autowired
    private SportService sportService;

    /**
     * 新增运动记录
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody SportDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        sportService.addSport(dto, userId);
        return Result.success(null, "运动记录添加成功");
    }

    /**
     * 分页查询运动记录
     */
    @GetMapping("/page")
    public Result<Page<Sport>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        Page<Sport> page = sportService.pageList(pageNum, pageSize, userId);
        return Result.success(page);
    }

    /**
     * 支持的运动项目及 MET 值
     */
    @GetMapping("/met-list")
    public Result<List<MetVO>> metList() {
        return Result.success(sportService.metList());
    }

    /**
     * 按 MET 公式计算消耗热量
     */
    @GetMapping("/calorie")
    public Result<CalorieCalcVO> calorie(@RequestParam String activity,
                                         @RequestParam Integer minutes,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(sportService.calcCalorie(activity, minutes, userId));
    }

    /**
     * 连续打卡天数与运动提醒
     */
    @GetMapping("/streak")
    public Result<SportStreakVO> streak(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(sportService.streak(userId));
    }

    /**
     * 导出某月运动记录 Excel
     */
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String month,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = (Long) request.getAttribute("loginUserId");
        YearMonth ym = ExcelExportUtil.parseMonth(month);
        List<Sport> records = sportService.listByMonth(ym, userId);

        List<List<String>> head = List.of(
                List.of("日期"), List.of("运动类型"), List.of("训练动作"), List.of("组数"),
                List.of("负重(kg)"), List.of("时长(分钟)"), List.of("消耗热量(kcal)"),
                List.of("酸痛部位"), List.of("备注"));

        List<List<Object>> rows = new ArrayList<>();
        int totalDuration = 0;
        double totalCalorie = 0;
        for (Sport s : records) {
            rows.add(Arrays.asList(
                    s.getRecordDate() == null ? "" : s.getRecordDate().toString(),
                    ExcelExportUtil.text(s.getSportType()),
                    ExcelExportUtil.text(s.getExerciseName()),
                    ExcelExportUtil.value(s.getSets()),
                    ExcelExportUtil.value(s.getWeight()),
                    ExcelExportUtil.value(s.getDuration()),
                    ExcelExportUtil.value(s.getConsumeCalorie()),
                    ExcelExportUtil.text(s.getSoreArea()),
                    ExcelExportUtil.text(s.getRemark())));
            totalDuration += s.getDuration() == null ? 0 : s.getDuration();
            totalCalorie += s.getConsumeCalorie() == null ? 0 : s.getConsumeCalorie();
        }
        if (!records.isEmpty()) {
            rows.add(Arrays.asList("合计", "", "", "", "", totalDuration,
                    Math.round(totalCalorie * 10) / 10.0, "", ""));
        }
        ExcelExportUtil.write(response, "运动记录_" + ym + ".xlsx", "运动记录", head, rows);
    }

    /**
     * 删除运动记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        sportService.removeByIdAndUserId(id, userId);
        return Result.success(null, "删除成功");
    }
}