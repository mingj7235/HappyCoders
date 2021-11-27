package com.happycoders.study;

import com.happycoders.account.CurrentAccount;
import com.happycoders.domain.Account;
import com.happycoders.domain.Study;
import com.happycoders.study.form.StudyForm;
import com.happycoders.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.ModuleElement;
import javax.print.MultiDoc;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Controller
public class StudyController {

    private final StudyService studyService;

    private final ModelMapper modelMapper;

    private final StudyFormValidator studyFormValidator;

    @InitBinder ("studyForm")
    public void studyFormInitBinder (WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    @GetMapping ("/new-study")
    public String newStudyForm (@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm ());
        return "study/form";
    }

    @PostMapping ("/new-study")
    public String newStudySubmit (@CurrentAccount Account account, @Valid StudyForm studyForm, Errors errors) {
        if (errors.hasErrors()) {
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);

    }

    @GetMapping ("/study")
    public String getStudy (@CurrentAccount Account account,
                            @Valid StudyForm studyForm,
                            Model model,
                            @RequestParam String path) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/" + path;
    }
}
