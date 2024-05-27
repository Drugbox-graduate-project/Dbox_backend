package com.drugbox.controller;

import com.drugbox.common.exception.CustomException;
import com.drugbox.common.exception.ErrorCode;
import com.drugbox.common.jwt.TokenDto;
import com.drugbox.common.oauth.platform.google.GoogleLoginParams;
import com.drugbox.dto.request.UserLoginRequest;
import com.drugbox.dto.response.IdResponse;
import com.drugbox.service.AuthService;
import com.drugbox.service.FCMTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final FCMTokenService fcmTokenService;

    @PostMapping("login/google")
    public ResponseEntity<TokenDto> googleLogin(@RequestBody GoogleLoginParams params){ // auth code
        checkFCMToken(params.getFcmToken());
        TokenDto response = authService.googleLogin(params);
        fcmTokenService.saveToken(response.getUserId(), params.getFcmToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/redirect/google") // 백엔드 자체 테스트용
    public void googleRedirect(@RequestParam("code") String authCode){
        log.info("\n  AuthCode:" + authCode);
        //return ResponseEntity.ok(authService.getGoogleAccessToken(authCode));
    }

    @PostMapping("/signup/pw")
    public ResponseEntity<IdResponse> signup(@RequestBody UserLoginRequest userLoginRequest) {
        Long userId = authService.signup(userLoginRequest);
        IdResponse response = IdResponse.builder()
                .id(userId)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/pw")
    public ResponseEntity<TokenDto> login(@RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(authService.login(userLoginRequest));
    }

    // AccessToken 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refreshToken(@RequestBody Map<String, String> refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken.get("refreshToken")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> accessToken){
        authService.logout(accessToken.get("accessToken"));
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/quit")
    public ResponseEntity<Void> quit(@RequestBody Map<String, String> accessToken){
        Long userId = authService.quit(accessToken.get("accessToken"));
        fcmTokenService.deleteToken(userId);
        return new ResponseEntity(HttpStatus.OK);
    }

    public void checkFCMToken(String fcmToken){
        if(fcmToken == null){
            throw new CustomException(ErrorCode.FCM_TOKEN_INVALID);
        }
    }
}
