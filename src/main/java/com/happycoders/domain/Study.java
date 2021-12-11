package com.happycoders.domain;

import com.happycoders.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withManagers", attributeNodes = {
        @NamedAttributeNode("managers")})
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER) // Lob 값이지만, 무조건 가져오게 하기 위해 eager로
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting() && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public String getImage() {
        return image != null ? image : "/images/default_banner.png";
    }

    public void publish() {
        if (this.closed || this.published) {
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 이미 공개했거나 종료되었습니다.");
        }
        this.published = true;
        this.publishedDateTime = LocalDateTime.now();
    }

    public void close() {
        if (this.closed || !this.published) {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 이미 종료되었거나 공개중이지 않습니다.");
        }
        this.closed = true;
        this.closedDateTime = LocalDateTime.now();
    }

    public void startRecruit() {
        if (this.canUpdateRecruiting()) {
            throw new RuntimeException("스터디 인원모집을 할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
        this.recruiting = true;
        this.recruitingUpdatedDateTime = LocalDateTime.now();
    }

    public void stopRecruit() {
        if(this.canUpdateRecruiting()) {
            throw new RuntimeException("스터디 인원모집을 종료할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
        this.recruiting = false;
        this.recruitingUpdatedDateTime = LocalDateTime.now();
    }

    public boolean canUpdateRecruiting() {
        return (!this.published || this.recruitingUpdatedDateTime != null) && !this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void updateNewPath(String newPath) {
        this.setPath(newPath);
    }

}
