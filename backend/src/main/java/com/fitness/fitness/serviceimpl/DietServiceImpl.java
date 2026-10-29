package com.fitness.fitness.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.dto.DietDTO;
import com.fitness.fitness.entity.Diet;
import com.fitness.fitness.entity.Food;
import com.fitness.fitness.entity.HealthRecord;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.DietMapper;
import com.fitness.fitness.mapper.FoodMapper;
import com.fitness.fitness.mapper.HealthRecordMapper;
import com.fitness.fitness.service.DietService;
import com.fitness.fitness.vo.DailyNutritionVO;
import com.fitness.fitness.vo.NutritionGoalVO;
import com.fitness.fitness.vo.RecipeRecommendVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class DietServiceImpl implements DietService {

    private final DietMapper dietMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final FoodMapper foodMapper;

    // 构造器注入，消除黄色警告，无需@Autowired
    public DietServiceImpl(DietMapper dietMapper, HealthRecordMapper healthRecordMapper, FoodMapper foodMapper) {
        this.dietMapper = dietMapper;
        this.healthRecordMapper = healthRecordMapper;
        this.foodMapper = foodMapper;
    }

    @Override
    public void addDiet(DietDTO dto, Long userId) {
        Diet diet = new Diet();
        diet.setUserId(userId);
        diet.setMealType(dto.getMealType());
        diet.setFoodName(dto.getFoodName());
        diet.setIntake(dto.getIntake());
        diet.setCalorie(dto.getCalorie());
        diet.setProtein(dto.getProtein());
        diet.setCarb(dto.getCarb());
        diet.setFat(dto.getFat());
        diet.setImageUrl(dto.getImageUrl());
        diet.setRecordDate(dto.getRecordDate());
        diet.setCreateTime(LocalDateTime.now());
        dietMapper.insert(diet);
    }

    @Override
    public Page<Diet> pageList(Long pageNum, Long pageSize, Long userId) {
        Page<Diet> page = new Page<>(pageNum, pageSize);
        return dietMapper.selectPage(page, Wrappers.<Diet>lambdaQuery()
                .eq(Diet::getUserId, userId)
                .orderByDesc(Diet::getRecordDate));
    }

    @Override
    public void removeByIdAndUserId(Long id, Long userId) {
        Diet diet = dietMapper.selectById(id);
        // 先判空，避免空指针
        if (diet == null) {
            throw new BusinessException("该饮食记录不存在");
        }
        if (!diet.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除他人记录");
        }
        dietMapper.deleteById(id);
    }

    @Override
    public DailyNutritionVO getDailyStats(LocalDate date, Long userId) {
        DailyNutritionVO vo = new DailyNutritionVO();
        List<Diet> list = dietMapper.selectList(Wrappers.<Diet>lambdaQuery()
                .eq(Diet::getUserId, userId)
                .eq(Diet::getRecordDate, date));
        if (list.isEmpty()) return vo;

        vo.setHasData(true);
        double c = 0, p = 0, co = 0, fa = 0;
        for (Diet d : list) {
            c += d.getCalorie() == null ? 0 : d.getCalorie();
            p += d.getProtein() == null ? 0 : d.getProtein();
            co += d.getCarb() == null ? 0 : d.getCarb();
            fa += d.getFat() == null ? 0 : d.getFat();
        }
        c = Math.round(c * 10) / 10.0;
        p = Math.round(p * 10) / 10.0;
        co = Math.round(co * 10) / 10.0;
        fa = Math.round(fa * 10) / 10.0;
        vo.setCalorie(c);
        vo.setProtein(p);
        vo.setCarb(co);
        vo.setFat(fa);

        // 营养失衡提醒（每日参考值，经验值）
        if (c > 2500) vo.getWarnings().add("总热量摄入偏高(>2500kcal)");
        if (c < 1200) vo.getWarnings().add("总热量摄入偏低(<1200kcal)");
        if (p > 150) vo.getWarnings().add("蛋白质摄入偏高(>150g)");
        if (p < 45) vo.getWarnings().add("蛋白质摄入偏低(<45g)");
        if (co > 350) vo.getWarnings().add("碳水化合物摄入偏高(>350g)");
        if (fa > 90) vo.getWarnings().add("脂肪摄入偏高(>90g)");
        return vo;
    }

    @Override
    public List<Diet> listByMonth(YearMonth month, Long userId) {
        return dietMapper.selectList(Wrappers.<Diet>lambdaQuery()
                .eq(Diet::getUserId, userId)
                .between(Diet::getRecordDate, month.atDay(1), month.atEndOfMonth())
                .orderByAsc(Diet::getRecordDate)
                .orderByAsc(Diet::getId));
    }

    @Override
    public NutritionGoalVO getDailyGoal(LocalDate date, Long userId) {
        NutritionGoalVO vo = new NutritionGoalVO();
        HealthRecord health = healthRecordMapper.selectOne(Wrappers.<HealthRecord>lambdaQuery()
                .eq(HealthRecord::getUserId, userId)
                .last("LIMIT 1"));

        double targetCalorie;
        double targetProtein;
        double targetCarb;
        double targetFat;

        if (health == null || health.getWeight() == null || health.getHeight() == null || health.getAge() == null) {
            // 健康档案不完整时按通用成人标准
            targetCalorie = 2000;
            targetProtein = 75;
            targetCarb = 250;
            targetFat = 56;
            vo.setBasis("未完善健康档案，按通用成人标准估算（2000 kcal）");
        } else {
            double weight = health.getWeight();
            boolean male = health.getGender() != null && health.getGender() == 1;
            // Mifflin-St Jeor 基础代谢 × 轻度活动系数
            double bmr = 10 * weight + 6.25 * health.getHeight() - 5 * health.getAge() + (male ? 5 : -161);
            double base = bmr * 1.375;

            String target = health.getTarget() == null ? "" : health.getTarget();
            double adjust;
            double proteinPerKg;
            if (target.contains("减脂")) {
                adjust = -400;
                proteinPerKg = 1.8;
            } else if (target.contains("增肌")) {
                adjust = 300;
                proteinPerKg = 2.0;
            } else {
                adjust = 0;
                proteinPerKg = 1.5;
            }

            targetCalorie = Math.max(1200, base + adjust);
            targetProtein = weight * proteinPerKg;
            // 脂肪占总热量 25%
            targetFat = targetCalorie * 0.25 / 9;
            // 剩余热量分配给碳水
            targetCarb = Math.max(0, (targetCalorie - targetProtein * 4 - targetFat * 9) / 4);
            vo.setBasis("按健康档案个性化估算（" + (male ? "男" : "女") + "，" + health.getAge() + "岁，"
                    + weight + "kg，目标：" + (target.isBlank() ? "未设置" : target) + "）");
        }

        vo.setTargetCalorie(r1(targetCalorie));
        vo.setTargetProtein(r1(targetProtein));
        vo.setTargetCarb(r1(targetCarb));
        vo.setTargetFat(r1(targetFat));

        DailyNutritionVO intake = getDailyStats(date, userId);
        double c = nz(intake.getCalorie());
        double p = nz(intake.getProtein());
        double co = nz(intake.getCarb());
        double fa = nz(intake.getFat());
        vo.setIntakeCalorie(c);
        vo.setIntakeProtein(p);
        vo.setIntakeCarb(co);
        vo.setIntakeFat(fa);

        vo.setCaloriePercent(percent(c, targetCalorie));
        vo.setProteinPercent(percent(p, targetProtein));
        vo.setCarbPercent(percent(co, targetCarb));
        vo.setFatPercent(percent(fa, targetFat));

        if (c <= 0) {
            vo.getWarnings().add("当日还没有饮食记录");
        } else {
            if (c > targetCalorie) {
                vo.getWarnings().add(String.format("今日热量已超出目标 %.0f kcal", c - targetCalorie));
            }
            if (p < targetProtein * 0.6) {
                vo.getWarnings().add(String.format("蛋白质摄入不足，还差 %.0f g", targetProtein - p));
            }
            if (fa > targetFat) {
                vo.getWarnings().add(String.format("今日脂肪已超出目标 %.0f g", fa - targetFat));
            }
            if (co > targetCarb) {
                vo.getWarnings().add(String.format("今日碳水已超出目标 %.0f g", co - targetCarb));
            }
        }
        return vo;
    }

    @Override
    public RecipeRecommendVO recommend(LocalDate date, Long userId, String mealType, Integer variant) {
        NutritionGoalVO goal = getDailyGoal(date, userId);
        RecipeRecommendVO vo = new RecipeRecommendVO();
        String meal = StringUtils.hasText(mealType) ? mealType : "午餐";
        vo.setMealType(meal);

        double remainCalorie = Math.max(0, goal.getTargetCalorie() - goal.getIntakeCalorie());
        double remainProtein = Math.max(0, goal.getTargetProtein() - goal.getIntakeProtein());
        double remainCarb = Math.max(0, goal.getTargetCarb() - goal.getIntakeCarb());
        double remainFat = Math.max(0, goal.getTargetFat() - goal.getIntakeFat());

        double ratio = switch (meal) {
            case "早餐" -> 0.25;
            case "晚餐" -> 0.3;
            case "加餐" -> 0.1;
            default -> 0.4;
        };

        double mealCalorie = remainCalorie * ratio;
        vo.setTargetCalorie(r1(mealCalorie));
        vo.setTargetProtein(r1(remainProtein * ratio));
        vo.setTargetCarb(r1(remainCarb * ratio));
        vo.setTargetFat(r1(remainFat * ratio));

        if (mealCalorie < 80) {
            vo.setTip("今日营养目标已基本完成，建议选择黄瓜、番茄等清淡蔬菜或一份水果作为加餐。");
            return vo;
        }

        int offset = variant == null ? 0 : Math.abs(variant);
        boolean snack = "加餐".equals(meal);
        boolean mainMeal = "午餐".equals(meal) || "晚餐".equals(meal);

        Food proteinFood;
        if (snack) {
            // 加餐优先选低热量的乳制品（牛奶/酸奶），避免奶酪等高热量食物
            List<Food> dairy = foodsByCategories("乳制品").stream()
                    .filter(f -> nz(f.getCalorie()) <= 100)
                    .toList();
            proteinFood = pick(dairy.isEmpty() ? foodsByCategories("乳制品") : dairy, offset);
        } else {
            proteinFood = pick(foodsByCategories("肉蛋"), offset);
        }
        Food carbFood = snack ? null : pick(foodsByCategories("谷薯"), offset);
        Food vegFood = mainMeal ? pick(foodsByCategories("蔬菜"), offset) : null;
        Food fruitFood = pick(foodsByCategories("水果"), offset);

        if (proteinFood != null && nz(proteinFood.getProtein()) > 0) {
            double grams = clamp(remainProtein * ratio * 0.9 / (nz(proteinFood.getProtein()) / 100.0), 60, 200);
            addItem(vo, proteinFood, round10(grams));
        }
        if (carbFood != null && nz(carbFood.getCarb()) > 0) {
            double grams = clamp(remainCarb * ratio * 0.7 / (nz(carbFood.getCarb()) / 100.0), 50, 250);
            addItem(vo, carbFood, round10(grams));
        }
        if (vegFood != null) {
            addItem(vo, vegFood, 200);
        }
        if (fruitFood != null) {
            addItem(vo, fruitFood, snack ? 100 : 150);
        }

        if (vo.getTotalCalorie() > mealCalorie * 1.15) {
            vo.setTip(snack ? "加餐份量略高于目标，可分两次食用。" : "本餐预计热量略高于目标，建议适当减少主食份量。");
        } else if (vo.getTotalCalorie() < mealCalorie * 0.8) {
            vo.setTip("本餐热量偏低，食量较大者可再增加一份主食或乳制品。");
        } else if (vo.getTotalProtein() < vo.getTargetProtein() * 0.6) {
            vo.setTip("蛋白质仍偏少，可再加一份酸奶或鸡蛋。");
        } else {
            vo.setTip(String.format("本餐约 %.0f kcal，已按剩余目标搭配，可直接打卡记录。", vo.getTotalCalorie()));
        }
        return vo;
    }

    private List<Food> foodsByCategories(String... categories) {
        return foodMapper.selectList(Wrappers.<Food>lambdaQuery()
                .in(Food::getCategory, (Object[]) categories)
                .orderByAsc(Food::getId));
    }

    private Food pick(List<Food> list, int offset) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int index = (offset + LocalDate.now().getDayOfYear()) % list.size();
        return list.get(index);
    }

    private void addItem(RecipeRecommendVO vo, Food food, double grams) {
        RecipeRecommendVO.RecipeItem item = new RecipeRecommendVO.RecipeItem();
        item.setFoodId(food.getId());
        item.setName(food.getName());
        item.setCategory(food.getCategory());
        item.setGrams(grams);
        double factor = grams / 100.0;
        item.setCalorie(r1(nz(food.getCalorie()) * factor));
        item.setProtein(r1(nz(food.getProtein()) * factor));
        item.setCarb(r1(nz(food.getCarb()) * factor));
        item.setFat(r1(nz(food.getFat()) * factor));
        vo.getItems().add(item);
        vo.setTotalCalorie(r1(vo.getTotalCalorie() + item.getCalorie()));
        vo.setTotalProtein(r1(vo.getTotalProtein() + item.getProtein()));
        vo.setTotalCarb(r1(vo.getTotalCarb() + item.getCarb()));
        vo.setTotalFat(r1(vo.getTotalFat() + item.getFat()));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round10(double value) {
        return Math.round(value / 10.0) * 10;
    }

    private double nz(Double value) {
        return value == null ? 0 : value;
    }

    private int percent(double intake, double target) {
        if (target <= 0) {
            return 0;
        }
        return (int) Math.round(intake * 100 / target);
    }

    private double r1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}