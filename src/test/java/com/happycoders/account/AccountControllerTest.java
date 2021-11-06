package com.happycoders.account;

import com.happycoders.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm () throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk()) //응답이 200인지
                .andExpect(view().name("account/sign-up")) //view 이름이 account/sign-up인지 확인
                .andExpect(model().attributeExists("signUpForm")); // attribute의 이름이 있는지를 확인하는 테스트
    }

    @DisplayName("회원 가입 처리 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "minjae")
                .param("email", "email..") //email valicdation 오류
                .param("password", "123456") // password validation 오류
                        .with(csrf()) //csrf를 넣어줘야 제대로된 테스트가 진행된다. // spring security 를 사용하여 form 테스트를 진행할때는 csrf 값을 전달해야한다.
        )
                .andDo(print())
                .andExpect(status().isOk()) //200을 기대했으나, csrf token때문에 403 오류가 뜨게된다.
                .andExpect(view().name("account/sign-up")); // 입력값이 오류이므로 view는 그대로 머물러 있다.
    }

    @DisplayName("회원 가입 처리 - 입력값 정상")
    @Test
    void signUpSubmit_with_right_input () throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "minjae")
                .param("email", "test@naver.com")
                .param("password", "23TTkk#213")
                .with(csrf())
        )
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        Account account = accountRepository.findByEmail("test@naver.com");

        assertNotNull(account);
        assertNotEquals(account.getPassword(), "23TTkk#213"); //password가 인코딩되었으므로 not equal 이어야한다.
        assertNotNull(account.getEmailCheckToken()); //token이 트랜잭션으로 잘 들어가서 생성되었는지 확인
        assertTrue(accountRepository.existsByEmail("test@naver.com"));
        then(javaMailSender).should().send(any(SimpleMailMessage.class)); //send가 호출되었는지 테스트
    }

}