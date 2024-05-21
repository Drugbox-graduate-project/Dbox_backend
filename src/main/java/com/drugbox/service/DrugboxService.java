package com.drugbox.service;

import com.drugbox.common.exception.CustomException;
import com.drugbox.common.exception.ErrorCode;
import com.drugbox.domain.*;
import com.drugbox.dto.request.DrugboxImageChangeRequest;
import com.drugbox.dto.request.DrugboxSaveRequest;
import com.drugbox.dto.response.DrugboxResponse;
import com.drugbox.dto.response.DrugboxSettingResponse;
import com.drugbox.dto.response.UserResponse;
import com.drugbox.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DrugboxService {
    private final DrugboxRepository drugboxRepository;
    private final UserRepository userRepository;
    private final UserDrugboxRepository userDrugboxRepository;
    private final NotificationRepository notificationRepository;
    private final InvitationRepository invitationRepository;
    private final ImageService imageService;
    private final NotificationService notificationService;


    // 구급상자 추가하기 (생성)
    public Long addDrugbox(DrugboxSaveRequest request, Long userId) throws IOException {
        User user = getUserOrThrow(userId);
        String imageUUID = checkImageUUID(request.getImage());
        Drugbox drugbox = Drugbox.createDrugbox(request.getName(), imageUUID);
        drugboxRepository.save(drugbox);

        UserDrugbox userDrugbox = UserDrugbox.createUserDrugbox(user, drugbox);
        userDrugboxRepository.save(userDrugbox);
        return drugbox.getId();
    }

    // 구급상자 추가하기 (초대코드)
    public Long addDrugboxByInviteCode(String inviteCode, Long userId){
        User user = getUserOrThrow(userId);
        Drugbox drugbox = drugboxRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DRUGBOX));
        checkIfUserIsDrugboxMember(user, drugbox);
        UserDrugbox userDrugbox = UserDrugbox.createUserDrugbox(user, drugbox);
        userDrugboxRepository.save(userDrugbox);
        sendNewMemberNotification(user, drugbox, "멤버 추가 알림",
                user.getNickname()+"님이 "+drugbox.getName()+"의 멤버로 추가되었습니다.");
        return drugbox.getId();
    }

    // 내 구급상자 리스트 조회
    public List<DrugboxResponse> getUserDrugboxes(Long userId) {
        getUserOrThrow(userId);
        List<Long> ids = userDrugboxRepository.findDrugboxIdByUserId(userId);
        return ids.stream()
                .map(id-> DrugboxToDrugboxResponse(getDrugboxOrThrow(id)))
                .collect(Collectors.toList());
    }

    // 구급상자 이름 변경하기
    public void changeDrugboxName(Long drugboxId, String name){
        Drugbox drugbox = getDrugboxOrThrow(drugboxId);
        drugbox.setName(name);
    }

    // 구급상자 사진 변경하기
    public void changeDrugboxImage(DrugboxImageChangeRequest request) throws IOException {
        Drugbox drugbox = getDrugboxOrThrow(request.getDrugboxId());
        String imageUUID = checkImageUUID(request.getImage());
        drugbox.setImage(imageUUID);
    }

    // 구급상자 세부설정 조회하기
    public DrugboxSettingResponse getDrugboxSetting(Long drugboxId) {
        Drugbox drugbox = getDrugboxOrThrow(drugboxId);
        List<UserResponse> users = drugbox.getUserDrugboxes().stream()
                .map(UserDrugbox::getUser)
                .map(user -> UserResponse.builder()
                        .nickname(user.getNickname())
                        .userId(user.getId())
                        .build())
                .collect(Collectors.toList());
        return DrugboxSettingResponse.builder()
                .name(drugbox.getName())
                .drugboxId(drugbox.getId())
                .image(imageService.processImage(drugbox.getImage()))
                .inviteCode(drugbox.getInviteCode())
                .users(users)
                .build();
    }

    // 닉네임으로 구급상자 초대하기
    public Long inviteUserToDrugbox(Long drugboxId, String nickname){
        User invitee = getUserOrThrow(nickname);
        Drugbox drugbox = getDrugboxOrThrow(drugboxId);
        checkIfUserIsDrugboxMember(invitee, drugbox);
        Invitation invitation = Invitation.builder()
                .invitedUser(invitee)
                .invitedDrugbox(drugbox)
                .build();
        invitationRepository.save(invitation);
        sendNotificationWithExtraInfo(invitee, "구급상자 초대",
                invitee.getNickname() + "님이 구급상자(" + drugbox.getName() + ")에 초대되었습니다.",
                "Invitation ID=" + invitation.getId());
        return invitation.getId();
    }

    // 구급상자 초대 수락하기
    public void acceptInvitation(Long invitationId, Long userId){
        Invitation invitation = getInvitationOrThrow(invitationId);
        User invitee = getUserOrThrow(userId);
        checkIfUserIsInvited(invitation, invitee);
        Drugbox drugbox = getDrugboxOrThrow(invitation.getInvitedDrugbox().getId());
        checkIfUserIsDrugboxMember(invitee, drugbox);
        UserDrugbox userDrugbox = UserDrugbox.createUserDrugbox(invitee, drugbox);
        userDrugboxRepository.save(userDrugbox);
        sendNewMemberNotification(invitee, drugbox, "멤버 추가 알림",
                invitee.getNickname()+"님이 "+drugbox.getName()+"의 멤버로 추가되었습니다.");
    }

    // 예외 처리 - 존재하는 User 인가
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    private User getUserOrThrow(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    // 예외 처리 - 존재하는 Drugbox 인가
    private Drugbox getDrugboxOrThrow(Long drugboxId) {
        return drugboxRepository.findById(drugboxId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_DRUGBOX));
    }

    private Invitation getInvitationOrThrow(Long invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INVITATION));
    }

    private DrugboxResponse DrugboxToDrugboxResponse(Drugbox drugbox){
        return DrugboxResponse.builder()
                .name(drugbox.getName())
                .drugboxId(drugbox.getId())
                .inviteCode(drugbox.getInviteCode())
                .image(imageService.processImage(drugbox.getImage()))
                .build();
    }

    private String checkImageUUID(MultipartFile image) throws IOException {
        String imageUUID = null;
        if (image!=null && !image.isEmpty()) {
            imageUUID = imageService.uploadImage(image);
        }
        return imageUUID;
    }

    private void checkIfUserIsDrugboxMember(User user, Drugbox drugbox){
        List<UserDrugbox> uds = drugbox.getUserDrugboxes();
        for(UserDrugbox ud: uds){
            if(ud.getUser() == user)
                throw new CustomException(ErrorCode.USER_ALREADY_DRUGBOX_MEMBER);
        }
    }

    private void checkIfUserIsInvited(Invitation invitation, User invitee){
        if(invitation.getInvitedUser() != invitee){
            throw new CustomException(ErrorCode.USER_NOT_INVITED);
        }
    }

    private void sendNotification(User user, String title, String message){
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();
        notificationService.makeNotification(notification);
    }

    private void sendNotificationWithExtraInfo(User user, String title, String message, String extraInfo){
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .extraInfo(extraInfo)
                .build();
        notificationService.makeNotification(notification);
    }

    private void sendNewMemberNotification(User exceptUser, Drugbox drugbox, String title, String message){
        drugbox.getUserDrugboxes().stream()
                .map(ud -> ud.getUser())
                .filter(u -> u != exceptUser)
                .forEach(u -> sendNotification(u, title, message));
    }
}
