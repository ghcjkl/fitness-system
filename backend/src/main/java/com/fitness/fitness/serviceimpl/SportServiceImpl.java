package com.fitness.fitness.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.dto.SportDTO;
import com.fitness.fitness.entity.HealthRecord;
import com.fitness.fitness.entity.Sport;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.HealthRecordMapper;
import com.fitness.fitness.mapper.SportMapper;
import com.fitness.fitness.service.SportService;
import com.fitness.fitness.vo.CalorieCalcVO;
import com.fitness.fitness.vo.MetVO;
import com.fitness.fitness.vo.SportStreakVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class SportServiceImpl implements SportService {
    @Autowired
    private SportMapper sportMapper;
    @Autowired
    private HealthRecordMapper healthRecordMapper;

    // 常见运动 MET 值（参考《体力活动纲要》Compendium of Physical Activities）
    private static final Map<String, Double> MET_MAP = new LinkedHashMap<>();
    static {
        MET_MAP.put("快走", 3.8);
        MET_MAP.put("慢跑", 7.0);
        MET_MAP.put("跑步", 8.3);
        MET_MAP.put("骑行", 6.8);
        MET_MAP.put("游泳", 7.0);
        MET_MAP.put("跳绳", 11.0);
        MET_MAP.put("瑜伽", 2.5);
        MET_MAP.put("力量训练", 5.0);
        MET_MAP.put("篮球", 6.5);
        MET_MAP.put("足球", 7.0);
        MET_MAP.put("羽毛球", 5.5);
        MET_MAP.put("乒乓球", 4.0);
        MET_MAP.put("爬楼梯", 8.0);
        MET_MAP.put("跳舞", 5.0);
        MET_MAP.put("家务", 3.0);
        MET_MAP.put("拉伸", 2.3);
    }

    @Override
    public void addSport(SportDTO dto, Long userId) {
        Sport sport = new Sport();
        sport.setUserId(userId);
        sport.setSportType(dto.getSportType());
        sport.setExerciseName(dto.getExerciseName());
        sport.setSets(dto.getSets());
        sport.setWeight(dto.getWeight());
        sport.setDuration(dto.getDuration());
        sport.setConsumeCalorie(dto.getConsumeCalorie());
        sport.setSoreArea(dto.getSoreArea());
        sport.setRemark(dto.getRemark());
        sport.setRecordDate(dto.getRecordDate());
        sport.setCreateTime(LocalDateTime.now());
        sportMapper.insert(sport);
    }

    @Override
    public Page<Sport> pageList(Long pageNum, Long pageSize, Long userId) {
        Page<Sport> page = new Page<>(pageNum, pageSize);
        return sportMapper.selectPage(page, Wrappers.<Sport>lambdaQuery()
                .eq(Sport::getUserId, userId)
                .orderByDesc(Sport::getRecordDate));
    }

    @Override
    public void removeByIdAndUserId(Long id, Long userId) {
        Sport sport = sportMapper.selectById(id);
        if (sport == null || !sport.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除");
        }
        sportMapper.deleteById(id);
    }

    @Override
    public List<Sport> listByMonth(YearMonth month, Long userId) {
        return sportMapper.selectList(Wrappers.<Sport>lambdaQuery()
                .eq(Sport::getUserId, userId)
                .between(Sport::getRecordDate, month.atDay(1), month.atEndOfMonth())
                .orderByAsc(Sport::getRecordDate)
                .orderByAsc(Sport::getId));
    }

    @Override
    public List<MetVO> metList() {
        List<MetVO> list = new ArrayList<>();
        MET_MAP.forEach((activity, met) -> list.add(new MetVO(activity, met)));
        return list;
    }

    @Override
    public CalorieCalcVO calcCalorie(String activity, Integer minutes, Long userId) {
        if (minutes == null || minutes <= 0) {
            throw new BusinessException("运动时长必须大于 0");
        }
        Double met = MET_MAP.get(activity);
        if (met == null) {
            throw new BusinessException("暂不支持该运动项目，请从列表中选择");
        }

        HealthRecord health = healthRecordMapper.selectOne(Wrappers.<HealthRecord>lambdaQuery()
                .eq(HealthRecord::getUserId, userId)
                .last("LIMIT 1"));
        double weight = 60;
        boolean estimated = true;
        if (health != null && health.getWeight() != null && health.getWeight() > 0) {
            weight = health.getWeight();
            estimated = false;
        }

        // 国际通用公式：kcal = MET × 3.5 × 体重(kg) / 200 × 时长(分钟)
        double calorie = met * 3.5 * weight / 200.0 * minutes;

        CalorieCalcVO vo = new CalorieCalcVO();
        vo.setActivity(activity);
        vo.setMet(met);
        vo.setMinutes(minutes);
        vo.setWeight(r1(weight));
        vo.setCalorie(r1(calorie));
        vo.setNote(estimated
                ? "健康档案未填写体重，按 60kg 估算，填写后更准确"
                : "按健康档案体重 " + r1(weight) + "kg 计算");
        return vo;
    }

    @Override
    public SportStreakVO streak(Long userId) {
        List<Sport> records = sportMapper.selectList(Wrappers.<Sport>lambdaQuery()
                .select(Sport::getRecordDate)
                .eq(Sport::getUserId, userId)
                .isNotNull(Sport::getRecordDate));
        TreeSet<LocalDate> dates = new TreeSet<>();
        for (Sport s : records) {
            if (s.getRecordDate() != null) {
                dates.add(s.getRecordDate());
            }
        }

        LocalDate today = LocalDate.now();
        // 只统计今天及以前，避免用户提前填写的未来日期影响连续天数
        List<LocalDate> valid = dates.stream()
                .filter(d -> !d.isAfter(today))
                .collect(Collectors.toList());

        boolean todayDone = dates.contains(today);
        // 今天还没打卡时，从昨天开始计算，避免早上打开页面就显示 0 天
        LocalDate cursor = todayDone ? today : today.minusDays(1);
        int currentStreak = 0;
        while (dates.contains(cursor)) {
            currentStreak++;
            cursor = cursor.minusDays(1);
        }

        int bestStreak = 0;
        int run = 0;
        LocalDate prev = null;
        for (LocalDate d : valid) {
            run = (prev != null && prev.plusDays(1).equals(d)) ? run + 1 : 1;
            bestStreak = Math.max(bestStreak, run);
            prev = d;
        }

        List<SportStreakVO.DayItem> week = new ArrayList<>();
        int weekDone = 0;
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            boolean done = dates.contains(d);
            boolean isToday = i == 0;
            if (done) {
                weekDone++;
            }
            week.add(new SportStreakVO.DayItem(d.toString(),
                    isToday ? "今天" : String.valueOf(d.getDayOfMonth()), done, isToday));
        }

        SportStreakVO vo = new SportStreakVO();
        vo.setCurrentStreak(currentStreak);
        vo.setBestStreak(bestStreak);
        vo.setTotalDays(valid.size());
        vo.setTodayDone(todayDone);
        vo.setWeekDone(weekDone);
        vo.setWeek(week);
        vo.setReminder(buildReminder(todayDone, currentStreak, valid.size(), weekDone));
        return vo;
    }

    private String buildReminder(boolean todayDone, int currentStreak, int totalDays, int weekDone) {
        if (todayDone) {
            return "今天已完成运动打卡，本周已运动 " + weekDone + " 天，保持节奏";
        }
        int hour = LocalDateTime.now().getHour();
        if (currentStreak > 0) {
            return hour >= 18
                    ? "已连续打卡 " + currentStreak + " 天，今天还没运动，睡前补一次别断了记录"
                    : "已连续打卡 " + currentStreak + " 天，今天动一动就能继续延续";
        }
        if (totalDays > 0) {
            return hour >= 18
                    ? "今天还没有运动记录，哪怕散步 20 分钟也算打卡"
                    : "今天还没有运动记录，抽 30 分钟动一动吧";
        }
        return "还没有运动记录，从今天的第一条打卡开始";
    }

    private double r1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}