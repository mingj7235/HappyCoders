package com.happycoders.study;

import com.happycoders.account.CurrentAccount;
import com.happycoders.domain.Account;
import com.happycoders.study.form.StudyForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudyController {
    @GetMapping ("/new-study")
    public String newStudyForm (@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm ());
        return "study/form";
    }
}
