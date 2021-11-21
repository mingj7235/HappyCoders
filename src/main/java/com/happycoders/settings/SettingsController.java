package com.happycoders.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happycoders.account.AccountService;
import com.happycoders.account.CurrentUser;
import com.happycoders.domain.Account;
import com.happycoders.domain.Tag;
import com.happycoders.settings.form.*;
import com.happycoders.settings.validator.NicknameValidator;
import com.happycoders.settings.validator.PasswordFormValidator;
import com.happycoders.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Controller
public class SettingsController {

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";

    static final String SETTINGS_PROFILE_URL = "/" + SETTINGS_PROFILE_VIEW_NAME;

    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";

    static final String SETTINGS_PASSWORD_URL = "/" + SETTINGS_PASSWORD_VIEW_NAME;

    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";

    static final String SETTINGS_NOTIFICATIONS_URL = "/" + SETTINGS_NOTIFICATIONS_VIEW_NAME;

    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";

    static final String SETTINGS_ACCOUNT_URL = "/" + SETTINGS_ACCOUNT_VIEW_NAME;

    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";

    static final String SETTINGS_TAGS_URL = "/" + SETTINGS_TAGS_VIEW_NAME;


    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final NicknameValidator nicknameValidator;

    private final TagRepository tagRepository;

    private final ObjectMapper objectMapper;

    //Validation을 위해 InitBinder를 사용한다.

    /**
     * InitBinder 의 움직임
     *
     * @InitBinder("DTO클래스명(앞의대문자는소문자로변경)") 에서 DTO를 처리할때,
     * addValidators에 추가한 Validator를 통해 검증하라는 명을 하면,
     * controller에서 @Valid를 통해, 해당 DTO에 해놓은 Validation과, InitBinder의 validator의 validation을 한다.
     * 그 후 처리할 해당 DTO 검증 결과가 Errors에 담기게되고, Errors.hasError 가 된다는 말은, 해당 validator에 걸린다는 말이다.
     */
    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(SETTINGS_PROFILE_URL)
    public String updateProfileForm(@CurrentUser Account account, Model model) {

        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model,
                                RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            // profile과 errors는 자동으로 model에 담겨 view tier로 간다.
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }

        accountService.updateProfile(account, profile);

        //redirect할때 잠깐 사용하는 데이터 -> view로 전달해준다.
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    //TODO : 비밀번호 다시 한 번 재 확인 후 비밀번호 변경 페이지로 이동 (패스워드 수정 강의에 있는 메모 확인 -> matchers 사용)
//    @GetMapping (SETTINGS_CHECK_PASSWORD_URL)
//    public String checkPasswordForm (@CurrentUser Account account, Model model) {
//        model.addAttribute(account);
//        model.addAttribute(new PasswordCheck());
//        return SETTINGS_CHECK_PASSWORD_VIEW_NAME;
//    }
//
//    @PostMapping (SETTINGS_CHECK_PASSWORD_URL)
//    public String checkPassword (@CurrentUser Account account,
//                                 @Valid @ModelAttribute PasswordCheck passwordCheck,
//                                 Errors errors, Model model) {
//
//    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account,
                                 @Valid @ModelAttribute PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {

            //Error 발생시에는 다시 모델에 account 정보를 담아서 돌려보내줘야한다.
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 수정했습니다.");
        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    @GetMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS_NOTIFICATIONS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_NOTIFICATIONS_URL)
    public String updateNotifications(@CurrentUser Account account,
                                      @Valid @ModelAttribute Notifications notifications, Errors errors, Model model,
                                      RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATIONS_VIEW_NAME;
        }
        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");

        return "redirect:" + SETTINGS_NOTIFICATIONS_URL;
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccountForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account,
                                @Valid @ModelAttribute NicknameForm nicknameForm,
                                Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }

    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute("whiteList", objectMapper.writeValueAsString(allTags));

        return SETTINGS_TAGS_VIEW_NAME;
    }

    //AJAX
    @PostMapping(SETTINGS_TAGS_URL + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
//        Tag tag = tagRepository.findByTitle(title)
//                .orElseGet(() -> tagRepository.save(Tag.builder()
//                .title(tagForm.getTagTitle())
//                .build()));
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(title).build());
        }
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();

    }

}















