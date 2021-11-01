package com.happycoders.account;

import com.happycoders.dto.AccountDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class AccountController {

    @GetMapping ("/sign-up")
    public String signUpForm (Model model) {
        return "account/sign-up";
    }
}
