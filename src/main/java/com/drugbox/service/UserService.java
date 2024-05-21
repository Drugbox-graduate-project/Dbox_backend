package com.drugbox.service;

import com.drugbox.common.exception.CustomException;
import com.drugbox.common.exception.ErrorCode;
import com.drugbox.domain.NotificationSetting;
import com.drugbox.domain.User;
import com.drugbox.dto.response.UserDetailResponse;
import com.drugbox.dto.response.UserEmailResponse;
import com.drugbox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserEmailResponse findUserInfoById(Long userId){
        return userRepository.findById(userId)
                .map(UserEmailResponse::of)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public UserEmailResponse findUserInfoByEmail(String email){
        return userRepository.findByEmail(email)
                .map(UserEmailResponse::of)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    public void giveUserRewardPoint(Long userId){
        User user = getUserOrThrow(userId);
        user.add100Point();
    }

    @Transactional(readOnly = true)
    public void checkNicknameDuplicate(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.EXIST_USER_NICKNAME);
        }
    }

    // 유저 이름 변경
    public void changeUserNickname(Long userId, String nickname){
        User user = getUserOrThrow(userId);
        checkNicknameAvailability(nickname);
        user.setNickname(nickname);
        userRepository.save(user);
    }

    // 리워드 포인트 확인
    public int getUserRewardPoints(Long userId){
        User user = getUserOrThrow(userId);
        return user.getPoint();
    }

    // 유저 정보 조회
    public UserDetailResponse getUserDetail(Long userId){
        User user = getUserOrThrow(userId);
        return UserDetailResponse.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }

    // 푸시 알림 설정 - 유통기한 알림
    public void changeIsExpDateNotificationEnabled(Long userId, boolean isExpDateNotificationEnabled){
        User user = getUserOrThrow(userId);
        NotificationSetting notificationSetting = user.getNotificationSetting();
        notificationSetting.setIsExpDateNotificationEnabled(isExpDateNotificationEnabled);
        userRepository.save(user);
    }

    // 푸시 알림 설정 - 폐기할 의약품 알림
    public void changeIsDrugDisposedNotificationEnabled(Long userId, boolean isDisposalDrugsNotificationEnabled){
        User user = getUserOrThrow(userId);
        NotificationSetting notificationSetting = user.getNotificationSetting();
        notificationSetting.setIsDrugDisposedNotificationEnabled(isDisposalDrugsNotificationEnabled);
        userRepository.save(user);
    }

    // 푸시 알림 설정 - 새 공지사항
    public void changeIsDrugAddedNotificationEnabled(Long userId, boolean isDrugAddedNotificationEnabled){
        User user = getUserOrThrow(userId);
        NotificationSetting notificationSetting = user.getNotificationSetting();
        notificationSetting.setIsDrugAddedNotificationEnabled(isDrugAddedNotificationEnabled);
        userRepository.save(user);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    private void checkNicknameAvailability(String nickname){
        if(userRepository.findByNickname(nickname).isPresent()){
            throw new CustomException(ErrorCode.EXIST_USER_NICKNAME);
        }
    }
}
