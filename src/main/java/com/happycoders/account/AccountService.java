package com.happycoders.account;

import com.happycoders.domain.Account;
import com.happycoders.settings.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    /**
     * saveNewAccount 에서 account는 builder를 통해 생성되고 JPA의 save를 통해 저장되었다.
     * 그 이후에 processNewAccount 메소드에서 generateEmailCheckToken()을 사용하여 token을 저장하려고 하였으나,
     * newAccount 객체는 이미 detached 상태다. 그러므로 processNewAccount 메소드에 @transactional을 붙여주어서 트랜젝션이 유지되도록 해야한다.
     * 그렇게된다면 newAccount 객체는 계속 persist 객체가 되고, DB와 계속 싱크가 되어준다.
     */

    public Account processNewAccount (SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) // password encoding
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyCreatedByWeb(true)
                .build();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("해피코더스, 회원 가입 인증"); //메일 제목
        mailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail()); //메일 본문
        javaMailSender.send(mailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))); //정석적으로 사용하는 방법은 아니지만 이렇게 사용하는게 간편하기도함
                // 왜이렇게 진행하는가? password를 인코딩된 것만 접근 가능하기때문에 (plain password에 접근할 수 없다.)
                // 정석적인 방법을 하려면 plain password 를 알아야한다.
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);

        //정석적인 방법
//        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                username, password //form에서 넘어온 username과 plain password를 사용하여 객체 생성
//        );
//        Authentication authenticate = authenticationManager.authenticate(token);
//        SecurityContext context = SecurityContextHolder.getContext();
//        context.setAuthentication(authenticate);

    }

    @Transactional (readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickName) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickName);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickName);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickName);
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setBio(profile.getBio());
        account.setLocation(profile.getLocation());

        //현재 파라미터로 받아왔던 account는 detached 된 녀석이다. (controller에서 http session을 통해 들어온 녀석)
        //그러므로 JPA에서 DB 변경을 해주기위해서는 save를 통해 merge를 시켜준다.
        accountRepository.save(account);
    }

}
