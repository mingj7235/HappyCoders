package com.happycoders.account;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

//Validator를 만들기위해서 Validator를 implements받는다.
public class SignUpFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object o, Errors errors) {

    }

}
