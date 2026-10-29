package com.fitness.fitness.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.fitness.entity.Diet;
import com.fitness.fitness.entity.HealthHistory;
import com.fitness.fitness.entity.Sport;
import com.fitness.fitness.mapper.DietMapper;
import com.fitness.fitness.mapper.HealthHistoryMapper;
import com.fitness.fitness.mapper.SportMapper;
import com.fitness.fitness.service.StatsService;
import com.fitness.fitness.vo.TrendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsServiceImpl implements StatsService {
    @Autowired
    private HealthHistoryMapper healthHistoryMapper;
    @Autowired
    private SportMapper sportMapper;
    @Autowired
    private DietMapper dietMapper;

    @Override
    public TrendVO trend(Long userId, Integer days) {
        int dayCount = (days == null || days <= 0) ? 30 : Math.min(days, 365);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(dayCount - 1L);

        Map<LocalDate, HealthHistory> healthMap = new HashMap<>();
        List<HealthHistory> histories = healthHistoryMapper.selectList(Wrappers.<HealthHistory>lambdaQuery()
                .eq(HealthHistory::getUserId, userId)
                .ge(HealthHistory::getRecordDate, start)
                .le(HealthHistory::getRecordDate, end));
        for (HealthHistory history : histories) {
            healthMap.put(history.getRecordDate(), history);
        }

        Map<LocalDate, Integer> sportMap = new HashMap<>();
        List<Sport> sports = sportMapper.selectList(Wrappers.<Sport>lambdaQuery()
                .eq(Sport::getUserId, userId)
                .ge(Sport::getRecordDate, start)
                .le(Sport::getRecordDate, end));
        for (Sport sport : sports) {
            int duration = sport.getDuration() == null ? 0 : sport.getDuration();
            sportMap.merge(sport.getRecordDate(), duration, Integer::sum);
        }

        Map<LocalDate, Double> calorieMap = new HashMap<>();
        List<Diet> diets = dietMapper.selectList(Wrappers.<Diet>lambdaQuery()
                .eq(Diet::getUserId, userId)
                .ge(Diet::getRecordDate, start)
                .le(Diet::getRecordDate, end));
        for (Diet diet : diets) {
            double calorie = diet.getCalorie() == null ? 0 : diet.getCalorie();
            calorieMap.merge(diet.getRecordDate(), calorie, Double::sum);
        }

        List<String> dates = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        List<Double> bodyFats = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        List<Double> calories = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            LocalDate date = start.plusDays(i);
            HealthHistory history = healthMap.get(date);
            dates.add(date.toString());
            weights.add(history == null ? null : history.getWeight());
            bodyFats.add(history == null ? null : history.getBodyFat());
            durations.add(sportMap.getOrDefault(date, 0));
            calories.add(round1(calorieMap.getOrDefault(date, 0.0)));
        }

        TrendVO vo = new TrendVO();
        vo.setDates(dates);
        vo.setWeight(weights);
        vo.setBodyFat(bodyFats);
        vo.setSportDuration(durations);
        vo.setCalorie(calories);
        return vo;
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
