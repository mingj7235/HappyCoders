package com.happycoders.main;

import com.happycoders.account.CurrentUser;
import com.happycoders.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    /**
     * @CurrentUser : 이 어노테이션이 붙은 파라미터가 'anonymousUser'라면 null값을 뱉고, 아니라면 account를 뱉는다.
     */
    public String home (@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }

        return "index";
    }
}
