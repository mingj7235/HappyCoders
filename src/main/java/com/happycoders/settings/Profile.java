package com.happycoders.settings;

import com.happycoders.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor //기본생성자가 없으면 만들어진 생성자로 접근을 하는데, 파라미터 값을 찾을 수 없을 경우 NPE발생
public class Profile {

    private String bio;

    private String url;

    private String occupation;

    private String location;

    public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
    }

}
