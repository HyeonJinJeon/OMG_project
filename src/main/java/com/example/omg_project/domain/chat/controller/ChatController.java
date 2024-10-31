package com.example.omg_project.domain.chat.controller;

import com.example.omg_project.domain.chat.entity.ChatMessage;
import com.example.omg_project.domain.chat.kafka.ChatMessageProducer;
import com.example.omg_project.domain.chat.service.ChatService;
import com.example.omg_project.domain.trip.service.TeamService;
import com.example.omg_project.domain.trip.service.TripService;
import com.example.omg_project.domain.user.entity.User;
import com.example.omg_project.domain.user.service.UserService;
import com.example.omg_project.global.jwt.util.JwtTokenizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final JwtTokenizer jwtTokenizer;
    private final UserService userService;
    private final ChatMessageProducer chatMessageProducer;


    @MessageMapping("/chat/{roomId}")
    public void sendMessageToRoom(String message, @DestinationVariable String roomId, SimpMessageHeaderAccessor headerAccessor) {
        String nickname = "Unknown user";
        String username = "Unknown user";
        String token = (String) headerAccessor.getSessionAttributes().get("jwtToken");
        System.out.println("토큰" + token);
        System.out.println("메세지" + message);

        // JWT 토큰에서 사용자 정보를 추출
        if (token != null && !jwtTokenizer.isAccessTokenExpired(token)) {
            username = jwtTokenizer.getUsernameFromToken(token);
            Optional<User> userOptional = userService.findByUsername(username);
            User user = userOptional.orElseThrow();
            nickname = user.getUsernick();
        }
        System.out.println("-------------------------------------------------------------------------------"+message);

        // Kafka로 메시지 전송
        chatMessageProducer.sendMessage(roomId, nickname + ": " + message);
    }

    /** TODO
     * 채팅방 화면을 렌더링하는 엔드포인트
     *
     * @param roomId 채팅방 ID
     * @param model  모델 객체, 뷰로 데이터를 전달하는 역할
     * @param request 클라이언트의 요청 정보를 담고 있는 HttpServletRequest 객체
     * @param redirectAttributes 리다이렉트 시에 데이터를 전달하기 위한 객체
     * @return 채팅방 화면을 반환하거나, 예외가 발생할 경우 홈 화면으로 리다이렉트
     */
    @GetMapping("/rooms/{roomId}")
    public String getChatRoom(@PathVariable("roomId") Long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            // 쿠키에서 accessToken 찾기
            String accessToken = jwtTokenizer.getAccessTokenFromCookies(request);

            // accessToken을 통해 user 객채 찾기
            String username = jwtTokenizer.getUsernameFromToken(accessToken);
            User user = userService.findByUsername(username).orElseThrow();

            try {
                // user객채와 채팅방 ID를 기반으로 사용자가 해당 채팅방에 참여하고 있는지 확인
                chatService.validateUserInChatRoom(roomId, user);
            }catch (RuntimeException e){
                e.printStackTrace();
            }

            String tripName = chatService.findTripName(roomId);

            model.addAttribute("roomId", roomId);
            model.addAttribute("tripName", tripName);
            model.addAttribute("user", user);

            return "chat/chat";  // 채팅 화면으로 이동

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        }
    }
}
