package com.fitness.fitness.controller;

import com.fitness.fitness.dto.DietDTO;
import com.fitness.fitness.entity.Diet;
import com.fitness.fitness.service.DietService;
import com.fitness.fitness.util.ExcelExportUtil;
import com.fitness.fitness.util.UploadFileUtil;
import com.fitness.fitness.vo.DailyNutritionVO;
import com.fitness.fitness.vo.NutritionGoalVO;
import com.fitness.fitness.vo.RecipeRecommendVO;
import com.fitness.fitness.vo.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/diet")
public class DietController {

    @Autowired
    private DietService dietService;

    @Autowired
    private UploadFileUtil fileUtil;

    /**
     * 上传餐食照片
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = fileUtil.uploadImg(file);
        return Result.success(url, "上传成功");
    }

    /**
     * 查询某日营养统计
     */
    @GetMapping("/stats")
    public Result<DailyNutritionVO> stats(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        DailyNutritionVO vo = dietService.getDailyStats(date == null ? LocalDate.now() : date, userId);
        return Result.success(vo);
    }

    /**
     * 查询某日营养目标与摄入对比
     */
    @GetMapping("/goal")
    public Result<NutritionGoalVO> goal(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(dietService.getDailyGoal(date == null ? LocalDate.now() : date, userId));
    }

    /**
     * 按剩余营养目标推荐一餐搭配
     */
    @GetMapping("/recommend")
    public Result<RecipeRecommendVO> recommend(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                               @RequestParam(required = false) String mealType,
                                               @RequestParam(defaultValue = "0") Integer variant,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        return Result.success(dietService.recommend(date == null ? LocalDate.now() : date, userId, mealType, variant));
    }

    /**
     * 新增饮食打卡
     */
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody DietDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        dietService.addDiet(dto, userId);
        return Result.success(null, "打卡成功");
    }

    /**
     * 分页查询本人饮食记录
     */
    @GetMapping("/page")
    public Result<Page<Diet>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                   @RequestParam(defaultValue = "10") Long pageSize,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        Page<Diet> page = dietService.pageList(pageNum, pageSize, userId);
        return Result.success(page);
    }

    /**
     * 导出某月饮食记录 Excel
     */
    @GetMapping("/export")
    public void export(@RequestParam(required = false) String month,
                       HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = (Long) request.getAttribute("loginUserId");
        YearMonth ym = ExcelExportUtil.parseMonth(month);
        List<Diet> records = dietService.listByMonth(ym, userId);

        List<List<String>> head = List.of(
                List.of("日期"), List.of("餐次"), List.of("食物名称"), List.of("食用量(g)"),
                List.of("热量(kcal)"), List.of("蛋白质(g)"), List.of("碳水(g)"), List.of("脂肪(g)"));

        List<List<Object>> rows = new ArrayList<>();
        double totalIntake = 0, totalCalorie = 0, totalProtein = 0, totalCarb = 0, totalFat = 0;
        for (Diet d : records) {
            rows.add(Arrays.asList(
                    d.getRecordDate() == null ? "" : d.getRecordDate().toString(),
                    ExcelExportUtil.text(d.getMealType()),
                    ExcelExportUtil.text(d.getFoodName()),
                    ExcelExportUtil.value(d.getIntake()),
                    ExcelExportUtil.value(d.getCalorie()),
                    ExcelExportUtil.value(d.getProtein()),
                    ExcelExportUtil.value(d.getCarb()),
                    ExcelExportUtil.value(d.getFat())));
            totalIntake += d.getIntake() == null ? 0 : d.getIntake();
            totalCalorie += d.getCalorie() == null ? 0 : d.getCalorie();
            totalProtein += d.getProtein() == null ? 0 : d.getProtein();
            totalCarb += d.getCarb() == null ? 0 : d.getCarb();
            totalFat += d.getFat() == null ? 0 : d.getFat();
        }
        if (!records.isEmpty()) {
            rows.add(Arrays.asList("合计", "", "",
                    round(totalIntake), round(totalCalorie), round(totalProtein), round(totalCarb), round(totalFat)));
        }
        ExcelExportUtil.write(response, "饮食记录_" + ym + ".xlsx", "饮食记录", head, rows);
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    /**
     * 删除单条饮食记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        dietService.removeByIdAndUserId(id, userId);
        return Result.success(null, "删除成功");
    }
}