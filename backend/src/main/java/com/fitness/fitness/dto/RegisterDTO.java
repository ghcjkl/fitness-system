package com.fitness.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 16, message = "用户名3-16位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码6-20位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String rePassword;

    private String nickname;
    private Integer gender;
    private Integer age;
    private Double height;
    private Double weight;
}