package com.happycoders.settings.form;

import com.happycoders.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor //기본생성자가 없으면 만들어진 생성자로 접근을 하는데, 파라미터 값을 찾을 수 없을 경우 NPE발생
public class Profile {

    @Length (max = 35)
    private String bio;

    @Length (max = 50)
    private String url;

    @Length(max = 50)
    private String occupation;

    @Length (max = 50)
    private String location;

    private String profileImage;


}
