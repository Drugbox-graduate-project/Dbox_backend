package com.drugbox.domain;

import com.drugbox.common.entity.BaseEntity;
import com.drugbox.common.oauth.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.drugbox.domain.Authority.ROLE_USER;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String password;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Authority authority = ROLE_USER;
    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;
    private String providerAccessToken;
    private String oauthId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserDrugbox> userDrugboxes = new ArrayList<>();

    @Column(unique = true, length = 20)
    private String nickname;
    private String email;
    private String image;
    @Builder.Default
    private int point = 0;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "notification_setting_id", referencedColumnName = "notification_setting_id")
    @Builder.Default
    private NotificationSetting notificationSetting = new NotificationSetting();

    public void setProviderAccessToken(String token){ this.providerAccessToken = token; }
    public void setImage(String image){ this.image = image; }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void add100Point(){ this.point += 100;}
}
