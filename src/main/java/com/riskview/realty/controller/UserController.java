package com.riskview.realty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.riskview.realty.domain.CustomUserDetails;
import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.service.UserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.util.Map;
import java.util.LinkedHashMap;

// 일반 사용자 컨트롤러(ROLE_USER)
@Controller
public class UserController {

    private final UserService userService;

    private Map<String, Object> validationRules;

    public UserController(UserService userService) {
        this.userService = userService;
        initializeValidationRules();
    }

    private void initializeValidationRules() {
        try (InputStream inputStream = getClass().getResourceAsStream("/static/js/validation-rules.json")) {
            ObjectMapper objectMapper = new ObjectMapper();
            validationRules = objectMapper.readValue(inputStream, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("유효성 검사 규칙 로드 실패: " + e.getMessage());
        }
    }

    private String validateField(String fieldName, String value) {
        if (validationRules == null || !validationRules.containsKey(fieldName)) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rule = (Map<String, Object>) validationRules.get(fieldName);
        if ((boolean) rule.getOrDefault("required", false) && (value == null || value.isEmpty())) {
            return (String) rule.get("message");
        }
        if (rule.containsKey("minLength") && value.length() < (int) rule.get("minLength")) {
            return (String) rule.get("message");
        }
        if (rule.containsKey("maxLength") && value.length() > (int) rule.get("maxLength")) {
            return (String) rule.get("message");
        }
        if (rule.containsKey("pattern") && !value.matches((String) rule.get("pattern"))) {
            return (String) rule.get("message");
        }

        return null;
    }

    /**
     * 로그인 페이지
     * @return 로그인 페이지
     */
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("userDTO", new UserDTO());
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
    public String showRegistrationForm(@ModelAttribute("userDTO") UserDTO userDTO, Model model) {
        /**
         * GET 요청 시 userDTO 객체를 모델에 추가하여 폼과 바인딩
         * 사용자가 입력한 데이터를 UserDTO 객체로 자동 매핑하기 위함
         * UserDTO 객체를 통해 초기값을 설정하는 효과도 있음
         * 유효성 검사에서 오류가 발생하면 BindingResult를 통해 오류 메시지를 userDTO와 함께 뷰로 전달하는 역할도 가능
         */
        model.addAttribute("userDTO", new UserDTO());
        //회원가입 페이지로 이동
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
    public String registerUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO, 
                             BindingResult bindingResult, 
                             @RequestParam("verificationCode") String verificationCode, 
                             HttpSession session, 
                             RedirectAttributes redirectAttributes, 
                             Model model) {
        System.out.println("\n[회원가입 요청]");
        System.out.println("▶ 사용자 ID: " + userDTO.getUserId());
        System.out.println("▶ 이메일: " + userDTO.getEmail());
        System.out.println("▶ 세션 ID: " + session.getId());
        System.out.println("[회원가입 요청 처리 시작]");
        System.out.println("▶ 전달된 인증 코드: " + verificationCode);
        System.out.println("▶ 전달된 사용자 정보: " + userDTO.toString());
        
        // 유효성 검사 실패 시 회원가입 페이지로 돌아감
        if (bindingResult.hasErrors()) {
            System.out.println("✘ 유효성 검사 실패");
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println("▶ 오류: " + error.getDefaultMessage())
            );
            model.addAttribute("userDTO", userDTO);
            return "user/register";
        }

        // 필드별 유효성 검사
        String emailError = validateField("email", userDTO.getEmail());
        String passwordError = validateField("password", userDTO.getPassword());
        String userIdError = validateField("userId", userDTO.getUserId());
        
        // 이메일, 비밀번호, 사용자 ID 중 하나라도 유효성 검사에 실패하면
        if (emailError != null || passwordError != null || userIdError != null) {
            System.out.println("✘ 유효성 검사 실패 (서버 측)");
            System.out.println("▶ 이메일 오류: " + emailError);
            System.out.println("▶ 비밀번호 오류: " + passwordError);
            System.out.println("▶ 사용자 ID 오류: " + userIdError);
            
            // 오류 메시지를 BindingResult에 추가
            if (emailError != null) {
                bindingResult.rejectValue("email", "invalid.email", emailError);
            }
            if (passwordError != null) {
                bindingResult.rejectValue("password", "invalid.password", passwordError);
            }
            if (userIdError != null) {
                bindingResult.rejectValue("userId", "invalid.userId", userIdError);
            }
            
            model.addAttribute("userDTO", userDTO);
            // 회원가입 페이지로 돌아감
            return "user/register";
        }

        // 유효성 검사 성공 시 회원가입 진행
        try {
            System.out.println("[회원가입 처리 시작]");
            // 회원정보를 등록
            userService.registerUser(userDTO, verificationCode, session);
            System.out.println("✔ 회원가입 성공");
            
            // 리다이렉트 후 전달할 메시지
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 성공적으로 완료되었습니다!");
            // 회원가입 성공 시 회원가입 성공 페이지로 이동
            return "user/register_success";
            
        } catch (IllegalArgumentException e) {
            System.out.println("✘ 회원가입 실패 (검증 오류)");
            System.out.println("▶ 오류 내용: " + e.getMessage());
            
            bindingResult.rejectValue("email", "invalid.verificationCode", e.getMessage());
            model.addAttribute("userDTO", userDTO);
            // 유효성 검사 실패 시 userDTO를 모델에 추가
            return "user/register";
            
        } catch (Exception e) {
            System.out.println("✘ 회원가입 실패 (시스템 오류)");
            System.out.println("▶ 오류 내용: " + e.getMessage());
            e.printStackTrace();
            
            bindingResult.rejectValue("email", "system.error", "회원가입 처리 중 오류가 발생했습니다.");
            model.addAttribute("userDTO", userDTO);
            // 회원가입 페이지로 돌아감
            return "user/register";
        }
    }

    /**
     * 인증코드 발송
     * @param email 사용자 이메일
     * @param session 사용자 세션
     * @return
     */
    @PostMapping(value = "/send-verification-code", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String sendVerificationCode(@RequestParam("email") String email, HttpSession session) {
        System.out.println("\n[이메일 인증 코드 발송 요청]");
        System.out.println("▶ 요청 이메일: " + email);
        System.out.println("▶ 세션 ID: " + session.getId());
        
        try {
            // 세션과 회원가입 중인 사용자의 이메일에 인증코드 발송 혹은 저장
            userService.sendVerificationCode(email, session);
            System.out.println("✔ 이메일 인증 코드 발송 성공");
            
            String response = "인증 코드가 발송되었습니다.";
            System.out.println("▶ 응답 데이터: " + response);
            // 인증 코드 발송 성공 메시지 반환
            return response;
            
        } catch (Exception e) {
            System.out.println("✘ 이메일 인증 코드 발송 실패");
            System.out.println("▶ 오류 내용: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 인증 코드 확인
     * @param code 사용자가 입력한 인증 코드
     * @param session 사용자 세션
     * @return "success" 또는 "invalid"
     */
    @PostMapping(value = "/verify-email-code", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String verifyEmailCode(@RequestParam("code") String code, HttpSession session) {
        System.out.println("\n[인증 코드 검증 요청]");
        System.out.println("▶ 세션 ID: " + session.getId());
        System.out.println("▶ 입력된 코드: [" + code + "]");
        
        String storedCode = (String) session.getAttribute("verificationCode");
        System.out.println("▶ 저장된 코드: [" + storedCode + "]");
        
        boolean isValid = (storedCode != null && storedCode.equals(code));
        System.out.println("▶ 검증 결과: " + (isValid ? "일치" : "불일치"));
        
        String result = isValid ? "success" : "invalid";
        System.out.println("▶ 응답 데이터: " + result);
        
        return result;
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

    /**
     * 회원탈퇴 페이지
     * @param model
     * @return 회원탈퇴 페이지
     */
    @GetMapping("/delete_account")
    public String showDeleteAccountForm(Model model) {
        /**
         * 로그인된 사용자의 정보를 가져옴
         * SecurityContextHolder.getContext(): 현재 HTTP 요청에 대한 보안 정보를 가져옴
         * getAuthentication(): 현재 사용자의 인증 정보를 가져옴
         * getPrincipal(): 실제 사용자 정보를 가지고 있는 Principal 객체를 가져옴(CustomUserDetails 객체를 반환)
         */
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 로그인된 사용자의 정보가 CustomUserDetails 객체인 경우
        if (principal instanceof CustomUserDetails) {
            // 로그인된 사용자의 사용자 코드를 가져옴
            String userCode = ((CustomUserDetails) principal).getUserCode();
            // 사용자 코드를 기반으로 사용자 정보를 가져옴
            UserDTO userDTO = userService.findByUserCode(userCode);
            // 사용자 정보를 모델에 추가
            model.addAttribute("userDTO", userDTO);
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        } else {
            System.out.println("CustomerDetails 객체가 아닙니다.");
            // 로그인되지 않은 사용자의 경우 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }
    }

    @PostMapping("/delete_account")
    public String deleteAccount(@Valid @ModelAttribute("userDTO") UserDTO userDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
        
        // userCode 필드에 오류가 있는지 확인합니다.
        boolean hasUserCodeError = bindingResult.hasFieldErrors("userCode");

        // userCode 필드에 오류가 있다면
        if (hasUserCodeError) {
            // userDTO를 모델에 추가
            model.addAttribute("userDTO", userDTO);
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        }
        
        // 유효성 검사(userCode) 통과 시 회원탈퇴 진행
        try {
            // 회원정보를 삭제
            userService.deleteAccount(userDTO);
            // SecurityContextHolder의 인증 정보를 제거
            SecurityContextHolder.clearContext();
            // HttpSession 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            // 성공 메시지를 flash attribute로 저장
            redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
            // 홈페이지로 리다이렉트
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            // 회원탈퇴 실패 시 (예: 인증 코드 관련 오류)
            bindingResult.rejectValue("email", "invalid.verificationCode", e.getMessage());
            model.addAttribute("userDTO", userDTO);
            return "user/delete_account";
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 메시지를 bindingResult에 추가
            bindingResult.rejectValue("email", "delete.error", "회원 탈퇴 중 오류가 발생했습니다.");
            model.addAttribute("userDTO", userDTO);
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        }
    }
}

