package com.happycoders.account;

import com.happycoders.account.form.SignUpForm;
import com.happycoders.config.AppProperties;
import com.happycoders.domain.Account;
import com.happycoders.domain.Tag;
import com.happycoders.domain.Zone;
import com.happycoders.mail.EmailMessage;
import com.happycoders.mail.EmailService;
import com.happycoders.settings.form.Notifications;
import com.happycoders.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.EntityGraph;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    /**
     * saveNewAccount 에서 account는 builder를 통해 생성되고 JPA의 save를 통해 저장되었다.
     * 그 이후에 processNewAccount 메소드에서 generateEmailCheckToken()을 사용하여 token을 저장하려고 하였으나,
     * newAccount 객체는 이미 detached 상태다. 그러므로 processNewAccount 메소드에 @transactional을 붙여주어서 트랜젝션이 유지되도록 해야한다.
     * 그렇게된다면 newAccount 객체는 계속 persist 객체가 되고, DB와 계속 싱크가 되어준다.
     */

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();

        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context(); // model 이라고 생각하면 된다. thymeleaf가 제공
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "HAPPY CODERS 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("해피코더스, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
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

    @Transactional(readOnly = true)
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
        modelMapper.map(profile, account);
        //현재 파라미터로 받아왔던 account는 detached 된 녀석이다. (controller에서 http session을 통해 들어온 녀석)
        //그러므로 JPA에서 DB 변경을 해주기위해서는 save를 통해 merge를 시ㅛ켜준다.
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword)); //password encoding
        accountRepository.save(account); //JPA merge
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account); //login을 해주지 않으면 navigation bar 에서 authentication 인증은 받아야하므로 ! 해야한다.
    }

    public void sendLoginLink(Account account) {
        Context context = new Context(); // model 이라고 생각하면 된다. thymeleaf가 제공
        context.setVariable("link", "/login- by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "HAPPY CODERS 로그인 링크");
        context.setVariable("message", "HAPPY CODERS에 로그인하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        account.generateEmailCheckToken();
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("HAPPY CODERS 로그인 링크")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public Set<Tag> getTags(Account account) {
//        Optional<Account> byId = accountRepository.findById(account.getId());
//        return byId.orElseThrow().getTags();
        Account byId = accountRepository.findAccountWithTagsById(account.getId());
        return byId.getTags();
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    // TODO : 관리자 기능 -> Tag 중복 or 참조 없는 것들 찾아서 수정하는 기능 구현할 것
    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
//        Optional<Account> byId = accountRepository.findById(account.getId());
//        return byId.orElseThrow().getZones();
        Account byId = accountRepository.findAccountWithZonesById(account.getId());
        return byId.getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if (account == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }

}
