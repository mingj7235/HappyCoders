package com.happycoders.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
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

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setDestinationNameTokenizer(NameTokenizers.UNDERSCORE) //지금 변수에는 inderscore로 쓰지 않았으므로 underscore일때만 토큰화하여 구분하도록 설정! 즉, 캐멀인경우는 하나의 변수로 인지
                .setSourceNameTokenizer(NameTokenizers.UNDERSCORE);
        return modelMapper;
    }

}
