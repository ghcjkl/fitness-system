package com.fitness.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Food;
import com.fitness.fitness.service.FoodService;
import com.fitness.fitness.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    /**
     * 食材搜索（饮食打卡联想 / 食材百科列表）
     */
    @GetMapping("/search")
    public Result<Page<Food>> search(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String category,
                                     @RequestParam(defaultValue = "1") Long pageNum,
                                     @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(foodService.search(keyword, category, pageNum, pageSize));
    }

    /**
     * 食材分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(foodService.listCategories());
    }

    /**
     * 食材详情
     */
    @GetMapping("/{id}")
    public Result<Food> detail(@PathVariable Long id) {
        return Result.success(foodService.getDetail(id));
    }
}
