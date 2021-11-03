package com.happycoders.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class AccountController {

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

        //TODO 회원 가입 처리 -> 리다이렉트로 루트로 돌림
        return "redirect:/";

    }
}
