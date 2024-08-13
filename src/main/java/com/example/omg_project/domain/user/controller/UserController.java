package com.example.omg_project.domain.user.controller;

import com.example.omg_project.domain.user.dto.request.Oauth2LoginDto;
import com.example.omg_project.domain.user.dto.request.UserEditDto;
import com.example.omg_project.domain.user.entity.User;
import com.example.omg_project.domain.user.service.UserService;
import com.example.omg_project.domain.user.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 일반 로그인 회원의 정보
     */
    @GetMapping("/my")
    public String index(Model model, Authentication authentication) {

        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "user/mypage";
        }
        return "redirect:/signin";
    }

    /**
     * OAuth2 로그인 회원 추가 정보 기입
     */
    @GetMapping("/oauthPage")
    public String addOauth2(Model model, Authentication authentication) {

        String username = authentication.getName();
        Optional<User> userOptional = userService.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 이미 추가 정보가 존재하는지 확인
            if (user.getPhoneNumber().equals("01000000000")) {
                model.addAttribute("user", user);
                return "user/oauth2page"; // 이미 정보가 있다면 마이 페이지로 리다이렉트
            }
            return "redirect:/my";
        }
        return "redirect:/signin";
    }

    @PostMapping("/oauthPage")
    public String addOauth2(Authentication authentication, @ModelAttribute Oauth2LoginDto oauth2LoginDto, RedirectAttributes redirectAttributes) {

        log.info("Received User: {}", oauth2LoginDto);
        String username = authentication.getName();
        try {
            Optional<User> updatedUser = userService.updateOauth2(username, oauth2LoginDto);
            if (updatedUser.isPresent()) {
                redirectAttributes.addFlashAttribute("msg", "추가정보가 저장되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("msg", "추가정보 저장에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "추가정보 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/oauthPage";
    }

    /**
     * 회원 정보 수정
     */
    @GetMapping("/my/profile")
    public String userEditForm(Model model, Authentication authentication) {

        String username = authentication.getName();

        Optional<User> userOptional = userService.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);
            model.addAttribute("username", username);

            return "/user/mypageEdit";
        }
        return "redirect:/signin";
    }

    /**
     * 회원 정보 수정 처리
     */
    @PostMapping("/my/profile")
    public String editUser(Authentication authentication, @ModelAttribute UserEditDto userEditDto, RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        try {
            Optional<User> updatedUser = userService.updateUser(username, userEditDto);
            if (updatedUser.isPresent()) {
                redirectAttributes.addFlashAttribute("msg", "회원정보가 수정되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("msg", "회원정보 수정에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "회원정보 수정 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/my/profile";
    }

    /**
     * 비밀번호 재발급 페이지 이동
     */
    @GetMapping("/my/change-password")
    public String showChangePasswordForm() {
        return "/user/findPassword";
    }
}