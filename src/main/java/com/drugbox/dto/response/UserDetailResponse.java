package com.drugbox.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDetailResponse {
    private String nickname;
    private String email;
}
