-- ========== 14. AI 方案表 ai_plan（饮食/运动方案） ==========
CREATE TABLE IF NOT EXISTS ai_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户id',
    plan_type VARCHAR(20) NOT NULL COMMENT '方案类型：DIET饮食/SPORT运动',
    content TEXT NOT NULL COMMENT '方案内容',
    sources TEXT NULL COMMENT 'RAG溯源引用(JSON)',
    create_time DATETIME NULL COMMENT '生成时间',
    KEY idx_user_type (user_id, plan_type, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI个性化饮食/运动方案';
