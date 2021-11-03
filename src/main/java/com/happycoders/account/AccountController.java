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

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

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

        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword()) //TODO encoding 해야한다.
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyCreatedByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken();

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("해피코더스, 회원 가입 인증"); //메일 제목
        mailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken() +
                            "&email=" + newAccount.getEmail()); //메일 본문
        javaMailSender.send(mailMessage);

        //TODO 회원 가입 처리 -> 리다이렉트로 루트로 돌림
        return "redirect:/";

    }
}
