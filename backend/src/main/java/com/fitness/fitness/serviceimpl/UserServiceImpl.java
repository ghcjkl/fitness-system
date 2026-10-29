package com.fitness.fitness.serviceimpl;

import com.fitness.fitness.dto.LoginDTO;
import com.fitness.fitness.dto.RegisterDTO;
import com.fitness.fitness.dto.UpdatePasswordDTO;
import com.fitness.fitness.dto.UpdateProfileDTO;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.UserMapper;
import com.fitness.fitness.service.UserService;
import com.fitness.fitness.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final String DEFAULT_ROLE = "USER";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(LoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordMatches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        // 兼容历史明文密码：校验通过后自动升级为BCrypt
        if (!isBcrypt(user.getPassword())) {
            user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getRePassword())) {
            throw new BusinessException("两次密码输入不一致");
        }
        User exist = userMapper.selectByUsername(dto.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setRole(DEFAULT_ROLE);
        user.setNickname(dto.getNickname());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        user.setHeight(dto.getHeight());
        user.setWeight(dto.getWeight());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(UpdateProfileDTO dto, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setNickname(dto.getNickname());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(UpdatePasswordDTO dto, Long userId) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!passwordMatches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(PASSWORD_ENCODER.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(String avatarUrl, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    // 校验密码：BCrypt密文用matches，历史明文用equals
    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return PASSWORD_ENCODER.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isBcrypt(String password) {
        return password != null && password.startsWith("$2");
    }
}