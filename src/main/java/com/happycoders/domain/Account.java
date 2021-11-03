package com.happycoders.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode (of = "id") // entity 들이 순환참조할때 stack over flow가 생길 수 있으므로 EqualsAndHashCode는 id만 사용함
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column (unique = true)
    private String email;

    @Column (unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified; // 이메일 인증여부

    //인증 토큰
    private String emailCheckToken;

    //가입한 날짜
    private LocalDateTime joinedAt;

    //자기소개글
    private String bio;

    //개인 웹사이트 주소
    private String url;

    //직업
    private String occupation;

    //사는 지역
    private String location; // varchar(255)

    @Lob @Basic (fetch = FetchType.EAGER)
    private String profileImage; // varchar (255)보다 커야하므로 lob으로

    //이벤트 발생의 여부 (알림을 이메일로 받을것인지 웹으로받을것인지의 대한 설정)
    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    public void generateEmailCheckToken() {
        //email token을 UUID로 random하게 한다.
        this.emailCheckToken = UUID.randomUUID().toString();
    }

}
