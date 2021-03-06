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
import com.happycoders.tag.TagForm;
import com.happycoders.tag.TagRepository;
import com.happycoders.tag.TagService;
import com.happycoders.zone.ZoneForm;
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

    private final TagService tagService;

    private final TagRepository tagRepository;

    private final ZoneRepository zoneRepository;

    //Validation??? ?????? InitBinder??? ????????????.

    /**
     * InitBinder ??? ?????????
     *
     * @InitBinder("DTO????????????(????????????????????????????????????)") ?????? DTO??? ????????????,
     * addValidators??? ????????? Validator??? ?????? ??????????????? ?????? ??????,
     * controller?????? @Valid??? ??????, ?????? DTO??? ????????? Validation???, InitBinder??? validator??? validation??? ??????.
     * ??? ??? ????????? ?????? DTO ?????? ????????? Errors??? ???????????????, Errors.hasError ??? ????????? ??????, ?????? validator??? ???????????? ?????????.
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
            // profile??? errors??? ???????????? model??? ?????? view tier??? ??????.
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);

        //redirect?????? ?????? ???????????? ????????? -> view??? ???????????????.
        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    //TODO : ???????????? ?????? ??? ??? ??? ?????? ??? ???????????? ?????? ???????????? ?????? (???????????? ?????? ????????? ?????? ?????? ?????? -> matchers ??????)
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

            //Error ??????????????? ?????? ????????? account ????????? ????????? ????????????????????????.
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "??????????????? ??????????????????.");
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
        attributes.addFlashAttribute("message", "?????? ????????? ??????????????????.");

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
        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
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
        Tag tag = tagService.findOrCreate(tagForm.getTagTitle());
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findOrCreate(tagForm.getTagTitle());
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















