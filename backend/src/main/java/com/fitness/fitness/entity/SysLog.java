package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 操作用户id
    private Long userId;
    // 操作用户名
    private String username;
    // 操作描述
    private String action;
    // 请求方法
    private String method;
    // 请求路径
    private String uri;
    // 客户端ip
    private String ip;
    // HTTP状态码
    private Integer status;
    // 操作时间
    private LocalDateTime createTime;
}
