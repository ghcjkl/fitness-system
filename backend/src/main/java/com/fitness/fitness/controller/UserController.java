package com.fitness.fitness.controller;

import com.fitness.fitness.dto.LoginDTO;
import com.fitness.fitness.dto.RegisterDTO;
import com.fitness.fitness.dto.UpdatePasswordDTO;
import com.fitness.fitness.dto.UpdateProfileDTO;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.service.UserService;
import com.fitness.fitness.util.JwtUtil;
import com.fitness.fitness.util.UploadFileUtil;
import com.fitness.fitness.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UploadFileUtil fileUtil;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Result<Void> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        String url = fileUtil.uploadImg(file);
        userService.updateAvatar(url, getLoginUserId(request));
        return Result.success(null, "头像上传成功");
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        return Result.success(token, "登录成功");
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success(null, "注册成功");
    }

    /**
     * 修改个人基础信息
     */
    @PostMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        userService.updateProfile(dto, getLoginUserId(request));
        return Result.success(null, "修改成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto, HttpServletRequest request) {
        userService.updatePassword(dto, getLoginUserId(request));
        return Result.success(null, "密码修改成功");
    }

    /**
     * 查询当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        return Result.success(userService.getUserInfo(getLoginUserId(request)));
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getLoginUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("loginUserId");
    }
}