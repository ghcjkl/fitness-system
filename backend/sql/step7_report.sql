USE fitness;

CREATE TABLE IF NOT EXISTS medical_report (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
  file_path VARCHAR(255) NOT NULL COMMENT '相对存储路径',
  file_type VARCHAR(20) NOT NULL COMMENT '文件类型：pdf/jpg/jpeg/png',
  file_size BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  report_date DATE NULL COMMENT '体检日期',
  ocr_text LONGTEXT NULL COMMENT 'OCR识别文本(阶段2使用)',
  status VARCHAR(20) NOT NULL DEFAULT 'DONE' COMMENT '处理状态：DONE/PENDING',
  create_time DATETIME NOT NULL COMMENT '上传时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告归档表';
