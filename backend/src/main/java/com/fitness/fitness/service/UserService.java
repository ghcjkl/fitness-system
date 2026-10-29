package com.fitness.fitness.service;

import com.fitness.fitness.dto.LoginDTO;
import com.fitness.fitness.dto.RegisterDTO;
import com.fitness.fitness.dto.UpdatePasswordDTO;
import com.fitness.fitness.dto.UpdateProfileDTO;
import com.fitness.fitness.entity.User;

public interface UserService {
    String login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getUserInfo(Long userId);
    void updateProfile(UpdateProfileDTO dto, Long userId);
    void updatePassword(UpdatePasswordDTO dto, Long userId);
    void updateAvatar(String avatarUrl, Long userId);
}