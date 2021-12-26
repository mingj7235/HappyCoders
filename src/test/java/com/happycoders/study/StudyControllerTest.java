package com.happycoders.study;

import com.happycoders.WithAccount;
import com.happycoders.account.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
class StudyControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected StudyService studyService;

    @Autowired
    protected StudyRepository studyRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Test
    @WithAccount("minjae")
    @DisplayName("스터디 개설 폼 조회")
    void createStudyForm () throws Exception {
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }
}
















