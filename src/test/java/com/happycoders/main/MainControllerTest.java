package com.happycoders.main;

import com.happycoders.account.AccountRepository;
import com.happycoders.account.AccountService;
import com.happycoders.account.SignUpForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test
 * - form submit post 요청시 -> .with(csrf()) 반드시 추가
 * - .andExpect(authenticated()) 또는 andExpect(unauthenticated())로 인증 여부를 확인 할 수 있음
 * - redirect 응답 : .andExpect(status().is3xxRedirection())으로 확인할 수 있음
 * - Junit5의 @BeforeEach와 @AfterEach
 * - 임의로 로그인 된 사용자가 필요한 경우에는 @WithMockUser 사용 (user, user)
 */

@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void addAccount() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("minjae");
        signUpForm.setEmail("3mins1@naver.com");
        signUpForm.setPassword("12345678");
        accountService.processNewAccount(signUpForm);
    }

    @AfterEach
    void deleteDuplication() {
        // @BeforeEach 코드에서 생성했던 계정정보를 각각의 테스트마다 실행 후 삭제하기 위함
        accountRepository.deleteAll();
    }


    @DisplayName("이메일로 로그인 테스트")
    @Test
    void login_with_email() throws Exception {

        mockMvc.perform(post("/login")
                        // username과 password 파라미터 name은 spring security가 정해놓은 파라미터 이름이다.
                        .param("username", "3mins1@naver.com")
                        .param("password", "12345678")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                /** 왜 nickname인 minjae로 로그인이 되는 것처럼 expect 되는가?
                 * UserAccount 의 생성자에서 부모인 super()의 첫번째 파라미터가 account.getNickname()을 주었기 때문이다.
                 */
                .andExpect(authenticated().withUsername("minjae"));
    }

    @DisplayName("닉네임으로 로그인 테스트")
    @Test
    void login_with_nickname() throws Exception {

        mockMvc.perform(post("/login")
                        // username과 password 파라미터 name은 spring security가 정해놓은 파라미터 이름이다.
                        .param("username", "minjae")
                        .param("password", "12345678")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                /** 왜 nickname인 minjae로 로그인이 되는 것처럼 expect 되는가?
                 * UserAccount 의 생성자에서 부모인 super()의 첫번째 파라미터가 account.getNickname()을 주었기 때문이다.
                 */
                .andExpect(authenticated().withUsername("minjae"));
    }

    @DisplayName("로그인 실패")
    @Test
    void login_fail() throws Exception {

        mockMvc.perform(post("/login")
                        .param("username", "not_registered")
                        .param("password", "not_registered")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @WithMockUser
    @DisplayName("로그아웃 테스트")
    @Test
    void logout() throws Exception {

        mockMvc.perform(post("/logout")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }


}