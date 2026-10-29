USE fitness;

CREATE TABLE IF NOT EXISTS community_comment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  dynamic_id BIGINT NOT NULL COMMENT '动态ID',
  user_id BIGINT NOT NULL COMMENT '评论用户ID',
  content VARCHAR(300) NOT NULL COMMENT '评论内容',
  create_time DATETIME NOT NULL COMMENT '评论时间',
  PRIMARY KEY (id),
  KEY idx_dynamic_id (dynamic_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区动态评论表';
