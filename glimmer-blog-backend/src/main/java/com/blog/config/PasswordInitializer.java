package com.blog.config;

import com.blog.model.User;
import com.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时确保存在默认管理员账号（仅在 admin 不存在时创建）
 */
@Component
public class PasswordInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordInitializer.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public PasswordInitializer(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 仅在 admin 账号不存在时创建，不覆盖已修改的密码
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            log.warn(">>> 已创建默认管理员 admin / admin123，请尽快修改密码！");
        }
    }
}
