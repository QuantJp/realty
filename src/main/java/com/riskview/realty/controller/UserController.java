package com.riskview.realty.controller;

import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.service.UserService;
import com.riskview.realty.support.UserDTOValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 일반 사용자 컨트롤러(ROLE_USER)
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // 사용자 정보 유효성 검사
    @Autowired
    private UserDTOValidator userDTOValidator;

    /**
     * 로그인 페이지
     * @return 로그인 페이지
     */
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    /**
     * 홈 페이지
     * @param model
     * @return 홈 페이지
     */
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }

    /**
     * 회원가입 페이지
     * @param model
     * @return 회원가입 페이지
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "user/register";
    }

    /**
     * 회원가입 프로세스
     * @param userDTO
     * @param bindingResult 유효성 검사 결과
     * @param verificationCode 인증코드
     * @param session 사용자 세션
     * @param redirectAttributes 리다이렉트 후 전달할 데이터
     * @return
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO, BindingResult bindingResult, @RequestParam("verificationCode") String verificationCode, HttpSession session, RedirectAttributes redirectAttributes) {
        userDTOValidator.validate(userDTO, bindingResult);
        // 유효성 검사 실패 시 회원가입 페이지로 돌아감
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        // 유효성 검사 성공 시 회원가입 진행
        try {
            // 회원정보를 등록
            userService.registerUser(userDTO, verificationCode, session);
            // 리다이렉트 후 전달할 메시지
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다!");
        } catch (IllegalArgumentException e) {
            // 인증코드가 올바르지 않을 때
            bindingResult.rejectValue("email", "invalid.verificationCode", e.getMessage());
            // 회원가입 페이지로 돌아감
            return "user/register";
        }
        // 회원가입 성공 시 회원가입 성공 페이지로 이동
        return "user/register_success";
    }

    /**
     * 인증코드 발송
     * @param email 사용자 이메일
     * @param session 사용자 세션
     * @return
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@RequestParam("email") String email, HttpSession session) {
        // 세션과 회원가입 중인 사용자의 이메일에 인증코드 발송 혹은 저장
        userService.sendVerificationCode(email, session);
        // HTTP 200 상태 코드와 함께 인증 코드 발송 성공 메시지 반환
        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    /**
     * 회원정보수정 페이지
     * @param model
     * @return 회원정보수정 페이지
     */
    @GetMapping("/modify")
    public String showModifyForm(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "user/modify";
    }
}

