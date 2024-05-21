package com.drugbox.service;

import com.drugbox.domain.Notification;
import com.drugbox.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FCMTokenService fcmTokenService;

    public void makeNotification(Notification notification){
        notificationRepository.save(notification);
        fcmTokenService.sendNotification(notification.getTitle(), notification.getMessage() , notification.getUser().getId());
    }
}
