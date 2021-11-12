package com.happycoders.settings;

import com.happycoders.WithAccount;
import com.happycoders.account.AccountRepository;
import com.happycoders.account.AccountService;
import com.happycoders.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void beforeEach() {

    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    //    @WithUserDetails(value = "minjae", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach 전에 실행이 되는 bug가 있다.
    @WithAccount(value = "minjae") //test를 위해서 만든 annotation
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("minjae");
        assertEquals(bio, account.getBio());
    }

    @WithAccount(value = "minjae") //test를 위해서 만든 annotation
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "길게 소개를 수정하는 경우 (35자이상 일시 오류가난다.) 길게 소개를 수정하는 경우 (35자이상 일시 오류가난다.) 길게 소개를 수정하는 경우 (35자이상 일시 오류가난다.) 길게 소개를 수정하는 경우 (35자이상 일시 오류가난다.)";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                        .param("bio", bio)
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("minjae");
        assertNull(account.getBio());
    }

    @WithAccount(value = "minjae") // 인증정보를 제공하기 위해서 !! 이게 있어야 한다.
    @DisplayName("프로필 수정폼")
    @Test
    void updateProfileForm() throws Exception {
        String bio = "짧은 소개 요청 ";

        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL)
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePassword_form () throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("패스워드 수정 - 입력값 정싱")
    @Test
    void updatePassword_success () throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "123456789")
                .with(csrf())
    )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account minjae = accountRepository.findByNickname("minjae");
        assertTrue(passwordEncoder.matches("123456789", minjae.getPassword()));
    }

    @WithAccount(value = "minjae")
    @DisplayName("패스워드 수정 - 입력값 에러 - 패스워드 불일치")
    @Test
    void updatePassword_fail () throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword", "123456789")
                .param("newPasswordConfirm", "1111111111")
                .with(csrf())
    )
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }


}