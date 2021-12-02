package com.happycoders.main;

import com.happycoders.account.CurrentAccount;
import com.happycoders.domain.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class MainController {

    @GetMapping("/")
    /**
     * @CurrentUser : 이 어노테이션이 붙은 파라미터가 'anonymousUser'라면 null값을 뱉고, 아니라면 account를 뱉는다.
     */
    public String home(@CurrentAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
            log.info("가입 직후 ");
            log.info(String.valueOf(account.isStudyUpdatedByWeb()));
        }


        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "/login";
    }

}
