package com.happycoders.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties ("app") //properties 파일에 설정해놓은 값
public class AppProperties {
    private String host;
}
