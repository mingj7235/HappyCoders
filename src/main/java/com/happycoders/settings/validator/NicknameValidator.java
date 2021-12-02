package com.happycoders.settings.validator;

import com.happycoders.account.AccountRepository;
import com.happycoders.domain.Account;
import com.happycoders.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return NicknameForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nicknameForm.getNickname());

        // 중복 체크
        if (byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임은 사용할 수 없습니다.");
        }
    }

}
