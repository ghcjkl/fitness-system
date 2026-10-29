package com.fitness.fitness.serviceimpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fitness.fitness.dto.HealthRecordDTO;
import com.fitness.fitness.entity.HealthHistory;
import com.fitness.fitness.entity.HealthRecord;
import com.fitness.fitness.mapper.HealthHistoryMapper;
import com.fitness.fitness.mapper.HealthRecordMapper;
import com.fitness.fitness.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {
    @Autowired
    private HealthRecordMapper healthRecordMapper;
    @Autowired
    private HealthHistoryMapper healthHistoryMapper;

    @Override
    public void saveRecord(HealthRecordDTO dto, Long userId) {
        HealthRecord record = healthRecordMapper.selectByUserId(userId);
        if (record == null) {
            record = new HealthRecord();
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            record.setAge(dto.getAge());
            record.setHeight(dto.getHeight());
            record.setWeight(dto.getWeight());
            record.setBodyFat(dto.getBodyFat());
            record.setGender(dto.getGender());
            record.setDisease(dto.getDisease());
            record.setTarget(dto.getTarget());
            record.setDietAvoid(dto.getDietAvoid());
            record.setAllergy(dto.getAllergy());
            record.setOldInjury(dto.getOldInjury());
            record.setUpdateTime(LocalDateTime.now());
            healthRecordMapper.insert(record);
        } else {
            record.setAge(dto.getAge());
            record.setHeight(dto.getHeight());
            record.setWeight(dto.getWeight());
            record.setBodyFat(dto.getBodyFat());
            record.setGender(dto.getGender());
            record.setDisease(dto.getDisease());
            record.setTarget(dto.getTarget());
            record.setDietAvoid(dto.getDietAvoid());
            record.setAllergy(dto.getAllergy());
            record.setOldInjury(dto.getOldInjury());
            record.setUpdateTime(LocalDateTime.now());
            healthRecordMapper.updateById(record);
        }
        // 保存当天体重/体脂快照，供趋势图使用
        saveSnapshot(userId, dto.getWeight(), dto.getBodyFat());
    }

    private void saveSnapshot(Long userId, Double weight, Double bodyFat) {
        LocalDate today = LocalDate.now();
        HealthHistory history = healthHistoryMapper.selectOne(Wrappers.<HealthHistory>lambdaQuery()
                .eq(HealthHistory::getUserId, userId)
                .eq(HealthHistory::getRecordDate, today));
        if (history == null) {
            history = new HealthHistory();
            history.setUserId(userId);
            history.setRecordDate(today);
            history.setWeight(weight);
            history.setBodyFat(bodyFat);
            history.setCreateTime(LocalDateTime.now());
            healthHistoryMapper.insert(history);
        } else {
            history.setWeight(weight);
            history.setBodyFat(bodyFat);
            healthHistoryMapper.updateById(history);
        }
    }

    @Override
    public HealthRecord getByUserId(Long userId) {
        return healthRecordMapper.selectByUserId(userId);
    }
}