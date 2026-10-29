-- 第一步：数据底座升级（在 DataGrip 中整体执行）
USE fitness;

-- 1. 用户角色（USER 普通用户 / ADMIN 管理员）
ALTER TABLE `user`
    ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN';

-- 2. 密码字段扩容，适配 BCrypt 密文（60 位）
ALTER TABLE `user`
    MODIFY COLUMN `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）';

-- 3. 健康档案补充维度
ALTER TABLE `health_record`
    ADD COLUMN `body_fat` DOUBLE NULL COMMENT '体脂率(%)',
    ADD COLUMN `allergy` VARCHAR(255) NULL COMMENT '过敏食材',
    ADD COLUMN `old_injury` VARCHAR(255) NULL COMMENT '运动旧伤';

-- 4. AI 对话溯源引用
ALTER TABLE `ai_chat`
    ADD COLUMN `sources` TEXT NULL COMMENT 'RAG 溯源引用 JSON';
