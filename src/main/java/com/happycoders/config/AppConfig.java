package com.happycoders.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    //passwordEncoder bean 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        //Bcrypt -> 해싱 알고리듬, 솔트 (salt)
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
