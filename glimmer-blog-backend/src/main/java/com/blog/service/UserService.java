package com.blog.service;

import com.blog.config.JwtUtil;
import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.PasswordRequest;
import com.blog.exception.BusinessException;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> BusinessException.unauthorized("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token);
    }

    public void changePassword(Long userId, PasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        // 注意：原密码错误必须保持 400，不能返回 401 —— 否则前端拦截器会把
        // 正在操作的管理员直接登出并跳转登录页。
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw BusinessException.badRequest("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
