package com.happycoders.config;

import com.happycoders.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // 내가 이제 시큐리티 설정을 하겠다!
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //UserDetails를 구현한 accountService 객체를 불러와준다. -> rememberme 에서 사용하기 위함
     private final AccountService accountService;

     private final DataSource dataSource;

    //WebSecurity Configurer Adapter에서 overriding하면된다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login", "/sign-up", "/check-email-token",
                        "/email-login", "/login-by-email").permitAll() //Get이든 Post든 모든 요청은 permitAll. 즉, 인증없이 권한없어도 들어올수있다.
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll() // profile 요청은 Get 요청만 모두에게 열려있음
                .anyRequest().authenticated(); //나머지요청은 로그인을 해야지 볼수있다.

        http.formLogin()
                .loginPage("/login").permitAll(); //loginPage custom config

        http.logout()
                .logoutSuccessUrl("/"); //logout 성공시 home으로 보내기

        //cookie를 통해 로그인 유지 및 보안
        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());

    }

    @Bean
    public PersistentTokenRepository tokenRepository () {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());

    }

}
