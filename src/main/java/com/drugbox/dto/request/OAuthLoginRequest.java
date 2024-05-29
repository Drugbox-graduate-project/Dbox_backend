package com.drugbox.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OAuthLoginRequest {
    @NotBlank // null, "", " "
    private String accessToken;
    @NotBlank
    private String fcmToken;
    @NotBlank
    private String idToken;
}
