package com.drugbox.common.oauth.platform.google;

import com.drugbox.common.oauth.OAuthLoginParams;
import com.drugbox.common.oauth.OAuthProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
@Getter
@Builder
@NoArgsConstructor
public class GoogleLoginParams implements OAuthLoginParams {
    private String authorizationCode;
    private String fcmToken;

    public GoogleLoginParams(String authorizationCode, String fcmToken) {
        this.fcmToken = fcmToken;
        this.authorizationCode = authorizationCode;
    }

    @Override
    public OAuthProvider oAuthProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public MultiValueMap<String, Object> makeBody() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        return body;
    }
}
