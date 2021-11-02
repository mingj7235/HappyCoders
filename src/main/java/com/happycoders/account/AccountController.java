package com.happycoders.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping ("/sign-up")
    public String signUpForm (Model model) {
//        model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm()); //이렇게 생략가능하다. 클래스의 이름을 camel로 바로 spring이 인식해서 view에서 object로 받을 수 있도록 해준다.
        return "account/sign-up";
    }
}
