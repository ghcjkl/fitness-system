package com.fitness.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Food;
import com.fitness.fitness.entity.SysLog;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.service.AdminService;
import com.fitness.fitness.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 用户列表
     */
    @GetMapping("/users")
    public Result<Page<User>> users(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = false) String keyword) {
        return Result.success(adminService.pageUsers(pageNum, pageSize, keyword));
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/user/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status,
                                     HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("loginUserId");
        adminService.updateUserStatus(id, status, adminId);
        return Result.success(null, status == 1 ? "已启用" : "已禁用");
    }

    /**
     * 重置用户密码为默认密码
     */
    @PostMapping("/user/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        adminService.resetPassword(id);
        return Result.success(null, "密码已重置为 123456");
    }

    /**
     * 管理员删除违规动态
     */
    @DeleteMapping("/community/{id}")
    public Result<Void> deleteCommunity(@PathVariable Long id) {
        adminService.deleteCommunity(id);
        return Result.success(null, "删除成功");
    }

    /**
     * 操作日志
     */
    @GetMapping("/logs")
    public Result<Page<SysLog>> logs(@RequestParam(defaultValue = "1") Long pageNum,
                                     @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(adminService.pageLogs(pageNum, pageSize));
    }

    /**
     * 食材库列表
     */
    @GetMapping("/foods")
    public Result<Page<Food>> foods(@RequestParam(defaultValue = "1") Long pageNum,
                                    @RequestParam(defaultValue = "10") Long pageSize,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String category) {
        return Result.success(adminService.pageFoods(pageNum, pageSize, keyword, category));
    }

    /**
     * 新增食材
     */
    @PostMapping("/food")
    public Result<Void> addFood(@RequestBody Food food) {
        adminService.saveFood(food);
        return Result.success(null, "新增成功");
    }

    /**
     * 修改食材
     */
    @PutMapping("/food/{id}")
    public Result<Void> updateFood(@PathVariable Long id, @RequestBody Food food) {
        adminService.updateFood(id, food);
        return Result.success(null, "修改成功");
    }

    /**
     * 删除食材
     */
    @DeleteMapping("/food/{id}")
    public Result<Void> deleteFood(@PathVariable Long id) {
        adminService.deleteFood(id);
        return Result.success(null, "删除成功");
    }
}
