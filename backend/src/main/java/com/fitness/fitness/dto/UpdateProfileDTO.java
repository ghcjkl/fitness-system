package com.fitness.fitness.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    // 昵称（性别/年龄/身高/体重等基础信息在健康档案中维护）
    private String nickname;
}