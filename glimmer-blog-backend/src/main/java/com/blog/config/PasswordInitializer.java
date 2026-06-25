package com.blog.config;

import com.blog.model.User;
import com.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时检查默认管理员密码，确保 admin123 可用
 */
@Component
public class PasswordInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public PasswordInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        userRepository.findByUsername("admin").ifPresent(user -> {
            // 验证当前密码是否正确，不正确则重置
            if (!passwordEncoder.matches("admin123", user.getPassword())) {
                user.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(user);
                System.out.println(">>> 管理员密码已重置为 admin123，请尽快修改！");
            }
        });
    }
}
