package com.fitness.fitness.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Community;
import com.fitness.fitness.entity.CommunityComment;
import com.fitness.fitness.entity.Food;
import com.fitness.fitness.entity.SysLog;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.CommunityCommentMapper;
import com.fitness.fitness.mapper.CommunityMapper;
import com.fitness.fitness.mapper.FoodMapper;
import com.fitness.fitness.mapper.SysLogMapper;
import com.fitness.fitness.mapper.UserMapper;
import com.fitness.fitness.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AdminServiceImpl implements AdminService {
    private static final String DEFAULT_PASSWORD = "123456";
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private CommunityCommentMapper commentMapper;
    @Autowired
    private SysLogMapper sysLogMapper;
    @Autowired
    private FoodMapper foodMapper;

    @Override
    public Page<User> pageUsers(Long pageNum, Long pageSize, String keyword) {
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<User>lambdaQuery()
                        .and(keyword != null && !keyword.isBlank(), q -> q
                                .like(User::getUsername, keyword)
                                .or()
                                .like(User::getNickname, keyword))
                        .orderByDesc(User::getCreateTime));
        page.getRecords().forEach(user -> user.setPassword(null));
        return page;
    }

    @Override
    public void updateUserStatus(Long targetId, Integer status, Long adminId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值不合法");
        }
        if (targetId.equals(adminId) && status == 0) {
            throw new BusinessException("不能禁用当前登录的账号");
        }
        User user = userMapper.selectById(targetId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long targetId) {
        User user = userMapper.selectById(targetId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(PASSWORD_ENCODER.encode(DEFAULT_PASSWORD));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void deleteCommunity(Long id) {
        Community community = communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException("动态不存在");
        }
        communityMapper.deleteById(id);
        commentMapper.delete(Wrappers.<CommunityComment>lambdaQuery()
                .eq(CommunityComment::getDynamicId, id));
    }

    @Override
    public Page<SysLog> pageLogs(Long pageNum, Long pageSize) {
        return sysLogMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<SysLog>lambdaQuery().orderByDesc(SysLog::getCreateTime));
    }

    @Override
    public Page<Food> pageFoods(Long pageNum, Long pageSize, String keyword, String category) {
        var query = Wrappers.<Food>lambdaQuery();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            query.and(w -> w.like(Food::getName, kw).or().like(Food::getAlias, kw));
        }
        query.eq(StringUtils.hasText(category), Food::getCategory, category);
        query.orderByAsc(Food::getCategory).orderByAsc(Food::getId);
        return foodMapper.selectPage(new Page<>(pageNum, pageSize), query);
    }

    @Override
    public void saveFood(Food food) {
        validateFood(food);
        String name = food.getName().trim();
        Long count = foodMapper.selectCount(Wrappers.<Food>lambdaQuery().eq(Food::getName, name));
        if (count != null && count > 0) {
            throw new BusinessException("该食材已存在");
        }
        food.setId(null);
        food.setName(name);
        food.setCreateTime(LocalDateTime.now());
        applyNutritionDefaults(food);
        foodMapper.insert(food);
    }

    @Override
    public void updateFood(Long id, Food food) {
        validateFood(food);
        if (foodMapper.selectById(id) == null) {
            throw new BusinessException("食材不存在");
        }
        String name = food.getName().trim();
        Long count = foodMapper.selectCount(Wrappers.<Food>lambdaQuery()
                .eq(Food::getName, name).ne(Food::getId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该食材名称已存在");
        }
        food.setId(id);
        food.setName(name);
        food.setCreateTime(null);
        applyNutritionDefaults(food);
        foodMapper.updateById(food);
    }

    @Override
    public void deleteFood(Long id) {
        if (foodMapper.selectById(id) == null) {
            throw new BusinessException("食材不存在");
        }
        foodMapper.deleteById(id);
    }

    private void validateFood(Food food) {
        if (food == null || !StringUtils.hasText(food.getName())) {
            throw new BusinessException("食物名称不能为空");
        }
        if (!StringUtils.hasText(food.getCategory())) {
            throw new BusinessException("请选择食材分类");
        }
        if (food.getCalorie() == null || food.getCalorie() < 0) {
            throw new BusinessException("热量必须填写且不能为负数");
        }
        if (food.getProtein() != null && food.getProtein() < 0) {
            throw new BusinessException("蛋白质不能为负数");
        }
        if (food.getCarb() != null && food.getCarb() < 0) {
            throw new BusinessException("碳水不能为负数");
        }
        if (food.getFat() != null && food.getFat() < 0) {
            throw new BusinessException("脂肪不能为负数");
        }
    }

    private void applyNutritionDefaults(Food food) {
        if (food.getProtein() == null) {
            food.setProtein(0.0);
        }
        if (food.getCarb() == null) {
            food.setCarb(0.0);
        }
        if (food.getFat() == null) {
            food.setFat(0.0);
        }
    }
}
