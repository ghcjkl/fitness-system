package com.fitness.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Food;
import com.fitness.fitness.entity.SysLog;
import com.fitness.fitness.entity.User;

public interface AdminService {
    Page<User> pageUsers(Long pageNum, Long pageSize, String keyword);
    void updateUserStatus(Long targetId, Integer status, Long adminId);
    void resetPassword(Long targetId);
    void deleteCommunity(Long id);
    Page<SysLog> pageLogs(Long pageNum, Long pageSize);

    /** 食材库分页查询 */
    Page<Food> pageFoods(Long pageNum, Long pageSize, String keyword, String category);
    /** 新增食材 */
    void saveFood(Food food);
    /** 修改食材 */
    void updateFood(Long id, Food food);
    /** 删除食材 */
    void deleteFood(Long id);
}
