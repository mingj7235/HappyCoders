package com.happycoders.account;

import com.happycoders.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Spring security와 여기서 사용하는 user정보의 중간 다리가 되어준다.
 *
 * 로그인할 때 account라는 principal을 security는 들고 있지 않다.
 * 그러므로 중간다리가 필요하다.
 * Spring Security가 다루는 유저정보와, domain 에서 다루는 유저정보의 사이의 갭을 매꾸어주는 어답터라고 생각하면 된다.
 */
@Getter
public class UserAccount extends User {
    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of (new SimpleGrantedAuthority("ROLE_USER")));
        //UserAccount의 필드인 account에 받아온 account를 넣어줘야한다.
        this.account = account;
    }



}
