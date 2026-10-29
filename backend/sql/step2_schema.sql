-- ========== 12. 健康指标历史表 health_history（体重/体脂趋势） ==========
CREATE TABLE IF NOT EXISTS health_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户id',
    weight DOUBLE NULL COMMENT '体重kg',
    body_fat DOUBLE NULL COMMENT '体脂率(%)',
    record_date DATE NOT NULL COMMENT '记录日期',
    create_time DATETIME NULL COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康指标历史';

-- 把当前已有档案补一条今天的快照，避免图表空白
INSERT IGNORE INTO health_history (user_id, weight, body_fat, record_date, create_time)
SELECT user_id, weight, body_fat, CURDATE(), NOW() FROM health_record;
