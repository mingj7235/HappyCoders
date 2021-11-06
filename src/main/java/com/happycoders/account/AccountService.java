package com.happycoders.account;

import com.happycoders.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    /**
     * saveNewAccount 에서 account는 builder를 통해 생성되고 JPA의 save를 통해 저장되었다.
     * 그 이후에 processNewAccount 메소드에서 generateEmailCheckToken()을 사용하여 token을 저장하려고 하였으나,
     * newAccount 객체는 이미 detached 상태다. 그러므로 processNewAccount 메소드에 @transactional을 붙여주어서 트랜젝션이 유지되도록 해야한다.
     * 그렇게된다면 newAccount 객체는 계속 persist 객체가 되고, DB와 계속 싱크가 되어준다.
     */
    @Transactional
    public void processNewAccount (SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);
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

    private void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("해피코더스, 회원 가입 인증"); //메일 제목
        mailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail()); //메일 본문
        javaMailSender.send(mailMessage);
    }

}
