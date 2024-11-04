package com.example.omg_project.domain.notification.service.impl;

import com.example.omg_project.domain.notification.entity.Notification;
import com.example.omg_project.domain.notification.repository.NotificationRepository;
import com.example.omg_project.domain.notification.service.NotificationService;
import com.example.omg_project.domain.user.entity.User;
import com.example.omg_project.global.exception.CustomException;
import com.example.omg_project.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 서비스의 구현체로, 알림 생성, 조회, 읽기 처리 및 알림 개수 조회를 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class NotificationImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 사용자를 위한 새로운 알림을 생성하고 Redis를 통해 실시간으로 전송합니다.
     *
     * @param user               알림을 받을 사용자
     * @param message            알림 메시지
     * @param notificationType  알림 유형
     * @param relatedEntityId   관련된 엔티티 ID (예: 게시물, 댓글 ID 등)
     */
    @Override
    public void createNotification(User user, String message, String notificationType, Long relatedEntityId) {
        try {
            // 알림 객체 생성
            Notification notification = new Notification();
            notification.setUserId(user.getId()); // 사용자 ID 설정
            notification.setMessage(message); // 알림 메시지 설정
            notification.setNotificationType(notificationType); // 알림 유형 설정
            notification.setCreatedAt(LocalDateTime.now()); // 알림 생성 시간 설정
            notification.setRelatedEntityId(relatedEntityId); // 관련된 엔티티 ID 설정

            notificationRepository.save(notification);

            // Redis를 통해 실시간 알림 전송
            redisTemplate.convertAndSend("notification", notification);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOTIFICATION_CREATION_ERROR);
        }
    }

    /**
     * 사용자의 읽지 않은 알림 목록을 조회합니다.
     *
     * @param user 사용자의 정보
     * @return 읽지 않은 알림 목록
     */
    @Override
    public List<Notification> getUserNotifications(User user) {
        try {
            // 사용자 ID를 기준으로 읽지 않은 알림 목록 조회
            return notificationRepository.findByUserIdAndIsReadFalse(user.getId());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOTIFICATION_RETRIEVAL_ERROR);
        }
    }

    /**
     * 특정 알림을 읽음 상태로 변경합니다.
     *
     * @param notificationId 읽음 상태로 변경할 알림 ID
     */
    @Override
    public void markAsRead(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

            notification.setRead(true);

            notificationRepository.save(notification);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOTIFICATION_UPDATE_ERROR);
        }
    }

    /**
     * 특정 사용자의 읽지 않은 알림 개수를 조회합니다.
     *
     * @param userId 사용자의 ID
     * @return 읽지 않은 알림의 개수
     */
    @Override
    public long getUnreadNotificationCount(Long userId) {
        try {
            return notificationRepository.countUnreadNotifications(userId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOTIFICATION_COUNT_ERROR);
        }
    }

    @Override
    public void markAllAsReadByType(Long userId, String type) {
        try {
            List<Notification> notificationList = notificationRepository.findUnreadNotificationsByUserIdAndType(userId, type);

            // 모든 알림을 읽음 상태로 변경
            for (Notification notification : notificationList) {
                notification.setRead(true);
            }

            // 일괄 저장하여 DB 접근 최소화
            notificationRepository.saveAll(notificationList);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOTIFICATION_UPDATE_ERROR);
        }
    }

}
