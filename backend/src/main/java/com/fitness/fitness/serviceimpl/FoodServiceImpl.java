package com.fitness.fitness.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Food;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.FoodMapper;
import com.fitness.fitness.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;

    @Override
    public Page<Food> search(String keyword, String category, Long pageNum, Long pageSize) {
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
    public List<String> listCategories() {
        return foodMapper.selectCategories();
    }

    @Override
    public Food getDetail(Long id) {
        Food food = foodMapper.selectById(id);
        if (food == null) {
            throw new BusinessException("食材不存在");
        }
        return food;
    }
}
