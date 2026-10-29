USE fitness;

-- 社区动态支持多图：最多 9 张，路径以逗号分隔存储
ALTER TABLE community MODIFY COLUMN img_url VARCHAR(1000) NULL COMMENT '图片路径，多张以逗号分隔';
