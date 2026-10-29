package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medical_report")
public class MedicalReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 所属用户id
    private Long userId;
    // 原始文件名
    private String fileName;
    // 相对存储路径，如 report/1/xxx.pdf
    private String filePath;
    // 文件类型：pdf/jpg/jpeg/png
    private String fileType;
    // 文件大小(字节)
    private Long fileSize;
    // 体检日期
    private LocalDate reportDate;
    // OCR识别文本（阶段2预留）
    private String ocrText;
    // 处理状态：DONE/PENDING
    private String status;
    private LocalDateTime createTime;
}
