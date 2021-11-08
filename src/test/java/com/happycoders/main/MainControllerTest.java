package com.happycoders.main;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test
 * - form submit post 요청시 -> .with(csrf()) 반드시 추가
 * - .andExpect(authenticated()) 또는 andExpect(unauthenticated())로 인증 여부를 확인 할 수 있음
 * - redirect 응답 : .andExpect(status().is3xxRedirection())으로 확인할 수 있음
 * - Junit5의 @BeforeEach와 @AfterEach
 * - 임의로 로그인 된 사용자가 필요한 경우에는 @WithMockUser 사용용 */
@SpringBootTest
@AutoConfigureMockMvc
class MainControllerTest {

    @
}