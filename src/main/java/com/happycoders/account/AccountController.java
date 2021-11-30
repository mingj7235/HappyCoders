package com.happycoders.account;

import com.happycoders.account.form.SignUpForm;
import com.happycoders.account.validator.SignUpFormValidator;
import com.happycoders.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;

    private final AccountService accountService;

    private final AccountRepository accountRepository;

    /**
     * InitBinder : form의 객체를 받을 때, 바인더를 사용해서 validator를 가동시킬 수 있다.
     *
     * @InitBinder의 (" signUpForm ") 은 SignUpForm 객체의 이름을 캐멀케이스화한 것과 매핑이되는 것이다.
     * signUpForm을 받을 때, 해당 validator 즉, signUpFormValidator를 추가했으므로 검증한다.
     */
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
//        model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm()); //이렇게 생략가능하다. 클래스의 이름을 camel로 바로 spring이 인식해서 view에서 object로 받을 수 있도록 해준다.
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) { //validation 에서 error를 받는 객체
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {

        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        // account 가 제대로 저장되지 않았을 경우
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        // account의 토큰이 맞지 않을 경우
//        if (!account.getEmailCheckToken().equals(token)) { //refactor
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        accountService.completeSignUp(account);
        // view에 전달
        model.addAttribute("numberOfUser", accountRepository.count()); // 몇번째 유저인지
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "/account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송 가능합니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentAccount Account account) {
        Account accountToView = accountService.getAccount(nickname);
        model.addAttribute(account); // 전달할 객체를 정해주지 않으면, 해당 타입인 Account의 캐멀케이스인 account 의 이름으로 들어간다.
        model.addAttribute("isOwner", accountToView.equals(account));
        return "account/profile";
    }

    @GetMapping ("/email-login")
    public String emailLoginForm () {
        return "account/email-login";
    }

    @PostMapping ("/email-login")
    public String sendEmailLoginLink (String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);

        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if(!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            return "account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping ("login-by-email")
    public String loginByEmail (String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        if (account == null || !account.isValidToken(token)) {
            model.addAttribute("error", "로그인 할 수 없습니다.");
            return "account/logged-in-by-email";
        }

        accountService.login(account);
        return "account/logged-in-by-email";
    }

}



















