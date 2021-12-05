package com.happycoders.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happycoders.account.AccountService;
import com.happycoders.account.CurrentAccount;
import com.happycoders.domain.Account;
import com.happycoders.domain.Tag;
import com.happycoders.domain.Zone;
import com.happycoders.settings.form.*;
import com.happycoders.settings.validator.NicknameValidator;
import com.happycoders.settings.validator.PasswordFormValidator;
import com.happycoders.tag.TagRepository;
import com.happycoders.zone.ZoneRepository;
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
@RequestMapping("/settings")
@RequiredArgsConstructor
@Controller
public class SettingsController {

    static final String ROOT = "/";

    static final String SETTINGS = "settings";

    static final String PROFILE = "/profile";

    static final String PASSWORD = "/password";

    static final String NOTIFICATIONS = "/notifications";

    static final String ACCOUNT = "/account";

    static final String TAGS = "/tags";

    static final String ZONES = "/zones";


    private final AccountService accountService;

    private final ModelMapper modelMapper;

    private final ObjectMapper objectMapper;

    private final NicknameValidator nicknameValidator;

    private final TagRepository tagRepository;

    private final ZoneRepository zoneRepository;

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

    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {

        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS + PROFILE;
    }

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model,
                                RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            // profile과 errors는 자동으로 model에 담겨 view tier로 간다.
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);

        //redirect할때 잠깐 사용하는 데이터 -> view로 전달해준다.
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + SETTINGS + PROFILE;
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

    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS + PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account,
                                 @Valid @ModelAttribute PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {

            //Error 발생시에는 다시 모델에 account 정보를 담아서 돌려보내줘야한다.
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 수정했습니다.");
        return "redirect:/" + SETTINGS + PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS + NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account,
                                      @Valid @ModelAttribute Notifications notifications, Errors errors, Model model,
                                      RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }
        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");

        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    }

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS + ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account,
                                @Valid @ModelAttribute NicknameForm nicknameForm,
                                Errors errors, Model model, RedirectAttributes attributes) {

        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());

        model.addAttribute("whiteList", objectMapper.writeValueAsString(allTags));

        return SETTINGS + TAGS;
    }

    //AJAX
    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {


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

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
//        Set<Zone> zones = accountService.getZones(account);
//       // System.out.println("zones stream : " + zones.stream().map(Zone::toString).collect(Collectors.toList()));
//        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
//
//        List<Zone> all = zoneRepository.findAll();
//       // System.out.println("zoneRepository raw data : "+all);
//        List<String> allZones = all.stream().map(Zone::toString).collect(Collectors.toList());
//        //System.out.println("zoneRepository mapped data : " + allZones);
//        String data = objectMapper.writeValueAsString(allZones);
//        //System.out.println("objectMapper data : " + data);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whiteList", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null)
            return ResponseEntity.badRequest().build();

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null)
            return ResponseEntity.badRequest().build();

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }


}















