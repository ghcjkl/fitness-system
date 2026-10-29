package com.fitness.fitness.service;

import com.fitness.fitness.dto.HealthRecordDTO;
import com.fitness.fitness.entity.HealthRecord;

public interface HealthRecordService {
    void saveRecord(HealthRecordDTO dto, Long userId);
    HealthRecord getByUserId(Long userId);
}