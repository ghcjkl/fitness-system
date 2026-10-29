-- ========== 15. 用户表 user：账号状态 ==========
ALTER TABLE `user`
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常/0禁用';

-- ========== 16. 操作日志表 sys_log ==========
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NULL COMMENT '操作用户id',
    username VARCHAR(50) NULL COMMENT '操作用户名',
    action VARCHAR(200) NULL COMMENT '操作描述',
    method VARCHAR(10) NULL COMMENT '请求方法',
    uri VARCHAR(200) NULL COMMENT '请求路径',
    ip VARCHAR(50) NULL COMMENT '客户端ip',
    status INT NULL COMMENT 'HTTP状态码',
    create_time DATETIME NULL COMMENT '操作时间',
    KEY idx_log_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志';

-- ========== 17. 设置管理员（把 username 换成你自己的账号） ==========
-- UPDATE `user` SET role = 'ADMIN' WHERE username = '你的用户名';

-- 查看当前账号角色
-- SELECT id, username, nickname, role, status FROM `user`;
