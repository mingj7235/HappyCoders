package com.happycoders.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity // 내가 이제 시큐리티 설정을 하겠다!
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //WebSecurity Configurer Adapter에서 overriding하면된다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login", "/sign-up", "/check-email", "/check-email-token",
                        "/email-login", "/check-email-login", "/login-link").permitAll() //Get이든 Post든 모든 요청은 permitAll. 즉, 인증없이 권한없어도 들어올수있다.
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll() // profile 요청은 Get 요청만 모두에게 열려있음
                .anyRequest().authenticated(); //나머지요청은 로그인을 해야지 볼수있다.

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());

    }

}
