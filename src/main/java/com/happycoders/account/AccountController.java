package com.happycoders.account;

import com.happycoders.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;

    private final AccountService accountService;

    private final AccountRepository accountRepository;

    /**
     * InitBinder : form의 객체를 받을 때, 바인더를 사용해서 validator를 가동시킬 수 있다.
     * @InitBinder의 ("signUpForm") 은 SignUpForm 객체의 이름을 캐멀케이스화한 것과 매핑이되는 것이다.
     * signUpForm을 받을 때, 해당 validator 즉, signUpFormValidator를 추가했으므로 검증한다.
     */
    @InitBinder ("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping ("/sign-up")
    public String signUpForm (Model model) {
//        model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm()); //이렇게 생략가능하다. 클래스의 이름을 camel로 바로 spring이 인식해서 view에서 object로 받을 수 있도록 해준다.
        return "account/sign-up";
    }

    @PostMapping ("/sign-up")
    public String signUpSubmit (@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) { //validation 에서 error를 받는 객체
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping ("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {

        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        // account 가 제대로 저장되지 않았을 경우
        if(account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        // account의 토큰이 맞지 않을 경우
//        if (!account.getEmailCheckToken().equals(token)) { //refactor
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }
        account.completeSignUp(); // refactor
        accountService.login(account);
        // view에 전달
        model.addAttribute("numberOfUser", accountRepository.count()); // 몇번째 유저인지
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

    @GetMapping ("/check-email")
    public String checkEmail (@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "/account/check-email";
    }

    @GetMapping ("/resend-confirm-email")
    public String resendConfirmEmail (@CurrentUser Account account, Model model) {
        if(!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송 가능합니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }





}
