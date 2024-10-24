package com.example.omg_project.domain.chat.service.impl;

import com.example.omg_project.domain.chat.service.BadWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class BadWordServiceImpl implements BadWordService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 주어진 메시지에서 비속어를 필터링하여 반환
     *
     * @param message 필터링할 메시지
     * @return 비속어가 필터링된 메시지
     */
    @Override
    public String filterMessage(String message) {
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> badWords = setOps.members("bad_words");  // Redis에서 비속어 Set 조회

        if (badWords != null) {
            for (String badWord : badWords) {
                if (message.contains(badWord)) {
                    String filterWord = "*".repeat(badWord.length());
                    message = message.replaceAll(badWord, filterWord);
                }
            }
        }

        return message;  // 필터링된 메시지 반환
    }
}
