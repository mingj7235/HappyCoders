package com.happycoders.settings;

import com.happycoders.account.AccountService;
import com.happycoders.account.CurrentUser;
import com.happycoders.domain.Account;
import com.happycoders.settings.form.Notifications;
import com.happycoders.settings.form.PasswordForm;
import com.happycoders.settings.form.Profile;
import com.happycoders.settings.validator.PasswordFormValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

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

    private final AccountService accountService;

    private final ModelMapper modelMapper;

    //Validation을 위해 InitBinder를 사용한다.
    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
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

}















