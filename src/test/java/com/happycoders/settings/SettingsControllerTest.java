package com.happycoders.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happycoders.WithAccount;
import com.happycoders.account.AccountRepository;
import com.happycoders.account.AccountService;
import com.happycoders.domain.Account;
import com.happycoders.domain.Tag;
import com.happycoders.domain.Zone;
import com.happycoders.tag.TagForm;
import com.happycoders.zone.ZoneForm;
import com.happycoders.tag.TagRepository;
import com.happycoders.zone.ZoneRepository;
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

import static com.happycoders.settings.SettingsController.*;
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

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder()
            .city("testCity")
            .localNameOfCity("testLocalNameOfCity")
            .province("testProvince")
            .build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount(value = "minjae")
    @DisplayName("????????? ?????? ?????? ???")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get("/" + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("????????? ?????? ??????")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/" + SETTINGS + TAGS + "/add")
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
    @DisplayName("????????? ?????? ??????")
    @Test
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("minjae");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account, newTag);

        assertTrue(account.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/" + SETTINGS + TAGS + "/remove")
                                .contentType(MediaType.APPLICATION_JSON)
//                .content("{\"tagTitle\": \"newTag\"}")
                                .content(objectMapper.writeValueAsString(tagForm))
                                .with(csrf())
                )
                .andExpect(status().isOk());
        assertFalse(account.getTags().contains(newTag));
    }


    //    @WithUserDetails(value = "minjae", setupBefore = TestExecutionEvent.TEST_EXECUTION) //@BeforeEach ?????? ????????? ?????? bug??? ??????.
    @WithAccount(value = "minjae") //test??? ????????? ?????? annotation
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateProfile() throws Exception {
        String bio = "?????? ????????? ???????????? ??????";

        mockMvc.perform(post("/" + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("minjae");
        assertEquals(bio, account.getBio());
    }

    @WithAccount(value = "minjae") //test??? ????????? ?????? annotation
    @DisplayName("????????? ???????????? - ????????? ??????")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "?????? ????????? ???????????? ?????? (35????????? ?????? ???????????????.) ?????? ????????? ???????????? ?????? (35????????? ?????? ???????????????.) ?????? ????????? ???????????? ?????? (35????????? ?????? ???????????????.) ?????? ????????? ???????????? ?????? (35????????? ?????? ???????????????.)";

        mockMvc.perform(post("/" + SETTINGS + PROFILE)
                        .param("bio", bio)
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account account = accountRepository.findByNickname("minjae");
        assertNull(account.getBio());
    }

    @WithAccount(value = "minjae") // ??????????????? ???????????? ????????? !! ?????? ????????? ??????.
    @DisplayName("????????? ?????????")
    @Test
    void updateProfileForm() throws Exception {
        String bio = "?????? ?????? ?????? ";

        mockMvc.perform(get("/" + SETTINGS + PROFILE)
                )
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("???????????? ?????? ???")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get("/" + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post("/" + SETTINGS + PASSWORD)
                        .param("newPassword", "123456789")
                        .param("newPasswordConfirm", "123456789")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account minjae = accountRepository.findByNickname("minjae");
        assertTrue(passwordEncoder.matches("123456789", minjae.getPassword()));
    }

    @WithAccount(value = "minjae")
    @DisplayName("???????????? ?????? - ????????? ?????? - ???????????? ?????????")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post("/" + SETTINGS + PASSWORD)
                        .param("newPassword", "123456789")
                        .param("newPasswordConfirm", "1111111111")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));

    }

    // TODO : ????????? ????????? ?????? ?????????

    @WithAccount(value = "minjae")
    @DisplayName("????????? ?????? ???")
    @Test
    void account_form() throws Exception {
        mockMvc.perform(get("/" + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("????????? ??????")
    @Test
    void updateAccount() throws Exception {
        Account account = accountRepository.findByNickname("minjae");

        assertEquals("minjae", account.getNickname());

//        mockMvc.perform(post(SettingsController.SETTINGS_ACCOUNT_URL)
//                .param("nickname", "updateNickname")
//                .with(csrf())
//        )
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl(SettingsController.SETTINGS_ACCOUNT_URL))
//                .andExpect(model().attributeExists("message"));
//
//        assertEquals("updateNickname", account.getNickname());
    }

    @WithAccount(value = "minjae")
    @DisplayName("?????? ?????? ?????? ???")
    @Test
    void Notifications_Form() throws Exception {
        mockMvc.perform(get("/" + SETTINGS + NOTIFICATIONS))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("notifications"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("?????? ?????? ??????")
    @Test
    void updateNotifications() throws Exception {

        Account account = accountRepository.findByNickname("minjae");
        assertTrue(account.isStudyUpdatedByWeb());
        assertTrue(account.isStudyCreatedByWeb());
        assertTrue(account.isStudyEnrollmentResultByWeb());
        assertFalse(account.isStudyUpdatedByEmail());
        assertFalse(account.isStudyCreatedByEmail());
        assertFalse(account.isStudyEnrollmentResultByEmail());

        mockMvc.perform(post("/" + SETTINGS + NOTIFICATIONS)
                        .param("studyUpdatedByWeb", "false")
                        .param("studyCreatedByWeb", "false")
                        .param("studyEnrollmentResultByWeb", "false")
                        .param("studyUpdatedByEmail", "true")
                        .param("studyCreatedByEmail", "true")
                        .param("studyEnrollmentResultByEmail", "true")
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + SETTINGS + NOTIFICATIONS))
                .andExpect(flash().attributeExists("message"));

        assertFalse(account.isStudyUpdatedByWeb());
        assertFalse(account.isStudyCreatedByWeb());
        assertFalse(account.isStudyEnrollmentResultByWeb());
        assertTrue(account.isStudyUpdatedByEmail());
        assertTrue(account.isStudyCreatedByEmail());
        assertTrue(account.isStudyEnrollmentResultByEmail());
    }

    @WithAccount(value = "minjae")
    @DisplayName("?????? ?????? ?????? ???")
    @Test
    void zones_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whiteList"));
    }

    @WithAccount(value = "minjae")
    @DisplayName("?????? ?????? ??????")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf())
                )
                .andExpect(status().isOk());

        Account minjae = accountRepository.findByNickname("minjae");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(minjae.getZones().contains(zone));
    }

    @WithAccount(value = "minjae")
    @DisplayName("?????? ?????? ??????")
    @Test
    void removeZone() throws Exception {
        Account minjae = accountRepository.findByNickname("minjae");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(minjae, zone);

        assertTrue(minjae.getZones().contains(zone));

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zoneForm))
                        .with(csrf())
                )
                .andExpect(status().isOk());

        assertFalse(minjae.getZones().contains(zone));
    }


}