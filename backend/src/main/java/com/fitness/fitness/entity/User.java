package com.fitness.fitness.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 用户名
    private String username;
    // 加密密码
    private String password;
    // 昵称
    private String nickname;
    // 头像地址
    private String avatar;
    // 角色：USER普通用户 / ADMIN管理员
    private String role;
    // 状态：1正常 0禁用
    private Integer status;
    // 性别 1男 2女
    private Integer gender;
    // 年龄
    private Integer age;
    // 身高cm
    private Double height;
    // 体重kg
    private Double weight;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
}