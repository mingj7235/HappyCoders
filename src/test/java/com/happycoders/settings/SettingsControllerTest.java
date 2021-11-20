package com.happycoders.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happycoders.WithAccount;
import com.happycoders.account.AccountRepository;
import com.happycoders.account.AccountService;
import com.happycoders.domain.Account;
import com.happycoders.domain.Tag;
import com.happycoders.settings.form.TagForm;
import com.happycoders.tag.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @BeforeEach
    void beforeEach() {

    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount(value = "minjae")
    @DisplayName("계정의 태그 수정 폼")
    @Test
    void updateTagsForm () throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/add")
                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"tagTitle\": \"newTag\"}")
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf())
        )
                .andExpect(status().isOk());
        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        assertTrue(accountRepository.findByNickname("minjae").getTags().contains(newTag));
    }

    @WithAccount(value = "minjae")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("minjae");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);

        assertTrue(account.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL + "/remove")
                                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"tagTitle\": \"newTag\"}")
                                .content(objectMapper.writeValueAsString(tagForm))
                                .with(csrf())
                )
                .andExpect(status().isOk());
        assertFalse(account.getTags().contains(newTag));
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

    // TODO : 닉네임 테스트 코드 만들기


//    @WithAccount(value = "minjae")
//    @DisplayName("알림 설정 수정")
//    @Test
//    void updateNotifications () {
//        mockMvc.perform(post(SettingsController.SETTINGS_NOTIFICATIONS_URL)
//                .with(csrt())
//        )
//                .andExpect(status().isOk())
//    }

}