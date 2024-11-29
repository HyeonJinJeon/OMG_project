package com.example.omg_project.domain.chat.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String BOOTSTRAP_SERVERS = "ec2-43-202-189-185.ap-northeast-2.compute.amazonaws.com/:9092"; // Kafka 서버 주소
    private static final String TOPIC_NAME_PREFIX = "chatTopic"; // 기본 토픽 이름
    private static final int PARTITION_COUNT = 10; // 파티션 수

    /**
     * 주어진 채팅방 ID와 메시지를 Kafka 토픽으로 전송
     *
     * @param roomId  메시지가 전송될 채팅방 ID
     * @param message 전송할 메시지 내용
     */
    public void sendMessage(String roomId, String message) {
        int roomNumber = Integer.parseInt(roomId);
        int topicSuffix = (roomNumber / PARTITION_COUNT) + 1;
        int partitionNumber = roomNumber % PARTITION_COUNT;

        String topicName = TOPIC_NAME_PREFIX + topicSuffix;

        // partitionNumber가 5일 경우 새로운 토픽 생성 확인
        if (partitionNumber == 5) {
            createTopicIfNotExists(TOPIC_NAME_PREFIX + (topicSuffix + 1), PARTITION_COUNT);
        }

        log.info("채팅방 ID: {}, 토픽: {}, 메시지: {}", roomId, topicName, message);

        // roomId와 메시지를 결합하여 최종 메시지 전송
        kafkaTemplate.send(topicName, partitionNumber, roomId, roomId + ":" + message);
    }

    /**
     * 주어진 토픽이 존재하지 않으면 새로 생성
     *
     * @param topicName 생성할 토픽 이름
     * @param partitionCount 파티션 수
     */
    private void createTopicIfNotExists(String topicName, int partitionCount) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(properties)) {
            if (isTopicExists(adminClient, topicName)) {
                log.info("토픽이 이미 존재합니다: {}", topicName);
                return;
            }
            createNewTopic(adminClient, topicName, partitionCount);
        } catch (Exception e) {
            log.error("토픽 확인/생성 에러 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 토픽 존재 여부 확인
     */
    private boolean isTopicExists(AdminClient adminClient, String topicName) {
        try {
            adminClient.describeTopics(Collections.singletonList(topicName)).values().get(topicName).get();
            return true;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.warn("토픽이 존재하지 않습니다: {}", topicName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("AdminClient 에러 발생: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 새 토픽 생성
     */
    private void createNewTopic(AdminClient adminClient, String topicName, int partitionCount) {
        NewTopic newTopic = new NewTopic(topicName, partitionCount, (short) 1);
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            log.info("새로운 토픽이 생성되었습니다: {}", topicName);
        } catch (ExecutionException | InterruptedException e) {
            log.error("토픽 생성 실패: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
