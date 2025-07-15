package com.riskview.realty.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.riskview.realty.domain.CustomUserDetails;
import com.riskview.realty.domain.dto.ModifyUserDTO;
import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.service.ModifyUserService;
import com.riskview.realty.service.UserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.LinkedHashMap;

// 일반 사용자 컨트롤러(ROLE_USER)
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final ModifyUserService modifyUserService;

    // 유효성 검사 규칙이 저장될 객체(이 코드에선 validation-rules.json 파일의 내용을 저장)
    private Map<String, Object> validationRules;

    /**
     * UserController 생성자
     * @param userService UserService 인터페이스 구현체
     */
    public UserController(UserService userService, ModifyUserService modifyUserService) {
        this.userService = userService;
        this.modifyUserService = modifyUserService;
        initializeValidationRules();
    }

    //=========================================================

    /**
     * 유효성 검사 규칙 초기화
     * validation-rules.json 파일을 읽어 유효성 검사 규칙을 로드
     * @throws Exception
     */
    private void initializeValidationRules() {
        // 현재 클래스의 정보를 Class 객체로 반환 -> validation-rules.json 파일을 찾고 읽어와 InputStream으로 반환
        try (InputStream inputStream = getClass().getResourceAsStream("/static/js/validation-rules.json")) {
            // JSON 파일을 읽어와 Map으로 변환하는 객체 생성
            ObjectMapper objectMapper = new ObjectMapper();
            /**
             * objectMapper.readValue(inputStream, ...)는 inputStream을 통해 읽어들인 JSON 데이터를 자바 객체로 변환해주는 핵심 부분이에요. 
             * new TypeReference<LinkedHashMap<String, Object>>() {}는 변환할 JSON 데이터가 어떤 자바 객체 형태인지 알려주는 역할을 해요.
             * 여기서는 키(key)가 문자열(String)이고 값(value)은 어떤 타입이든 될 수 있는(Object) 순서가 유지되는 맵(LinkedHashMap)으로 변환하겠다는 의미예요.
             */
            validationRules = objectMapper.readValue(inputStream, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("유효성 검사 규칙 로드 실패: " + e.getMessage());
        }
    }

    //=========================================================

    /**
     * 필드별 유효성 검사
     * @param fieldName
     * @param value
     * @return 
     */
    private String validateField(String fieldName, String value) {
        // 유효성 검사 규칙(validationRules) 자체가 준비되어 있는지,
        // 현재 검사하려는 fieldName에 대한 규칙이 validationRules 안에 정의되어 있는지 확인
        if (validationRules == null || !validationRules.containsKey(fieldName)) {
            return null;
        }

        // '타입 캐스팅' 관련 경고 무시
        @SuppressWarnings("unchecked")
        // fieldName에 대한 유효성 검사 규칙 가져오기
        Map<String, Object> rule = (Map<String, Object>) validationRules.get(fieldName);
        
        // 필수 값이 비어있으면 메시지 표시
        if ((boolean) rule.getOrDefault("required", false) && (value == null || value.isEmpty())) {
            return (String) rule.get("message");
        }
        
        // 최소 길이보다 짧으면 메시지 표시
        if (rule.containsKey("minLength") && value.length() < (int) rule.get("minLength")) {
            return (String) rule.get("message");
        }
        
        // 최대 길이보다 길면 메시지 표시
        if (rule.containsKey("maxLength") && value.length() > (int) rule.get("maxLength")) {
            return (String) rule.get("message");
        }
        
        // 정규 표현식과 일치하지 않으면 메시지 표시
        if (rule.containsKey("pattern") && !value.matches((String) rule.get("pattern"))) {
            return (String) rule.get("message");
        }

        return null;
    }

    //=========================================================

    /**
     * 로그인 페이지
     * @return 로그인 페이지
     */
    @GetMapping("/login")
    public String login(Model model) {
        // 로그인 폼을 사용자에게 보여주기 전에, 폼에 입력될 데이터를 받을 그릇(UserDTO 객체)을 미리 준비해서 뷰(로그인 페이지)로 전달
        model.addAttribute("userDTO", new UserDTO());
        // 로그인 페이지로 이동
        return "user/login";
    }

    //=========================================================

    /**
     * 회원가입 페이지
     * @param model
     * @return 회원가입 페이지
     */
    @GetMapping("/register")
    public String showRegistrationForm(@ModelAttribute("userDTO") UserDTO userDTO, Model model) {
        // 회원가입 폼을 사용자에게 보여주기 전에, 폼에 입력될 데이터를 받을 그릇(UserDTO 객체)을 미리 준비해서 뷰(회원가입 페이지)로 전달
        model.addAttribute("userDTO", new UserDTO());
        //회원가입 페이지로 이동
        return "user/register";
    }

    //=========================================================

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
        
        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            System.out.println("✘ 유효성 검사 실패");
            // 오류 메시지 출력
            bindingResult.getAllErrors().forEach(error -> 
                System.out.println("▶ 오류: " + error.getDefaultMessage())
            );
            // 유효성 검사 실패 시 사용자가 입력했던 데이터가 담겨있는 UserDTO 객체를 모델에 추가하여 폼에 입력된 값 유지
            model.addAttribute("userDTO", userDTO);
            // 회원가입 페이지로 돌아감
            return "user/register";
        }

        // 필드별 유효성 검사
        String emailError = validateField("email", userDTO.getEmail());
        String passwordError = validateField("password", userDTO.getPassword());
        String userIdError = validateField("userId", userDTO.getUserId());
        
        /**
         * 이메일, 비밀번호, 사용자 ID 중 하나라도 유효성 검사에 실패하면
         */
        if (emailError != null || passwordError != null || userIdError != null) {
            System.out.println("✘ 유효성 검사 실패 (서버 측)");
            System.out.println("▶ 이메일 오류: " + emailError);
            System.out.println("▶ 비밀번호 오류: " + passwordError);
            System.out.println("▶ 사용자 ID 오류: " + userIdError);
            
            // 유형별 에러코드 바인딩
            if (emailError != null) {
                bindingResult.rejectValue("email", "invalid.email", emailError);
            }
            if (passwordError != null) {
                bindingResult.rejectValue("password", "invalid.password", passwordError);
            }
            if (userIdError != null) {
                bindingResult.rejectValue("userId", "invalid.userId", userIdError);
            }
            // 유효성 검사 실패 시 사용자가 입력했던 데이터가 담겨있는 UserDTO 객체를 모델에 추가하여 폼에 입력된 값 유지
            model.addAttribute("userDTO", userDTO);
            // 회원가입 페이지로 돌아감(오류 메시지와 함께)
            return "user/register";
        }
        
        System.out.println("✔ 유효성 검사 성공");


        /**
         * 유효성 검사 성공 시 회원가입 진행
         */
        try {
            System.out.println("[회원가입 처리 시작]");
            // 이메일로 보낸 인증코드와 함께 회원정보 등록
            userService.registerUser(userDTO, verificationCode, session);
            System.out.println("✔ 회원가입 성공");
            
            // 리다이렉트 후 회원가입 성공 메시지 전달
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
            
            // 유효성 검사 실패 시 userDTO를 모델에 추가
            bindingResult.rejectValue("email", "system.error", "회원가입 처리 중 오류가 발생했습니다.");
            model.addAttribute("userDTO", userDTO);
            // 회원가입 페이지로 돌아감
            return "user/register";
        }
    }

    //=========================================================

    /**
     * 인증코드 발송
     * @param email 사용자 이메일
     * @param session 사용자 세션
     * @return
     * produces: 서버가 클라이언트에 어떤 미디어 타입을 생성하고 보낼 것인지 명시
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

    //=========================================================

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

    //=========================================================

    /**
     * 회원정보수정 페이지
     * @param model
     * @return 회원정보수정 페이지
     */
    @GetMapping("/modify")
    public String showModifyForm(Model model, HttpSession session) {
        // 로그인된 사용자의 정보를 가져옴
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // 로그인된 사용자의 정보가 CustomUserDetails 객체인 경우
        if (principal instanceof CustomUserDetails) {
            // 로그인된 사용자의 사용자 ID를 가져옴
            String userId = ((CustomUserDetails) principal).getUserId();
            String email = ((CustomUserDetails) principal).getEmail();
            // 사용자 ID로 사용자 정보를 가져옴
            UserDTO userDTO = userService.findByUserId(userId);
            // 사용자 정보를 세션에 추가
            session.setAttribute("user", userDTO);

            ModifyUserDTO modifyUserDTO = new ModifyUserDTO();
            modifyUserDTO.setUserId(userId);
            modifyUserDTO.setEmail(email);
            modifyUserDTO.setName(userDTO.getName());
            modifyUserDTO.setUserNickname(userDTO.getUserNickname());
            modifyUserDTO.setCurrentPassword(userDTO.getPassword());
            modifyUserDTO.setNewPassword(userDTO.getPassword());
            // 사용자 정보를 모델에 추가
            model.addAttribute("userDTO", userDTO);
            model.addAttribute("modifyUserDTO", modifyUserDTO);
            // 회원정보수정 페이지로 이동
            return "user/modify";
        } else {
            // 로그인되지 않은 사용자의 경우 로그인 페이지로 리다이렉트
            return "redirect:/user/login";
        }
    }

    //=========================================================

    /**
     * 회원정보수정
     * @param modifyUserDTO 사용자 정보
     * @param bindingResult 유효성 검사 결과
     * @param redirectAttributes 리다이렉트 후 전달할 데이터
     * @param model 모델
     * @param request HTTP 요청
     * @return 회원정보수정 페이지
     */
    @PostMapping("/modify")
    public String modifyUser(
        @Valid @ModelAttribute("modifyUserDTO") ModifyUserDTO modifyUserDTO,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model,
        HttpServletRequest request
    ) {
        // 현재 로그인된 사용자 정보 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof CustomUserDetails) {
            String loggedInUserId = ((CustomUserDetails) principal).getUserId();

            // 유효성 검사 실패 시
            if (bindingResult.hasErrors()) {
                System.out.println("✘ 회원정보 수정 유효성 검사 실패");
                bindingResult.getAllErrors().forEach(error -> 
                    System.out.println("▶ 오류: " + error.getDefaultMessage())
                );
                model.addAttribute("modifyUserDTO", modifyUserDTO);
                return "user/modify";
            }
            
            // 현재 로그인된 사용자와 수정하려는 사용자가 동일하지 않을 시
            if (!loggedInUserId.equals(modifyUserDTO.getUserId())) {
                bindingResult.rejectValue("userId", "invalid.user", "다른 사용자의 정보를 수정할 수 없습니다.");
                model.addAttribute("modifyUserDTO", modifyUserDTO);
                return "user/modify";
            }
            
            try {
                modifyUserService.modifyUserInfo(modifyUserDTO, request.getSession());
                redirectAttributes.addFlashAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
                return "redirect:/user/modify";
            } catch (ModifyUserService.InvalidPasswordException e) {
                bindingResult.rejectValue("currentPassword", "invalid.password", "현재 비밀번호가 일치하지 않습니다.");
                model.addAttribute("modifyUserDTO", modifyUserDTO);
            } catch (ModifyUserService.PasswordMismatchException e) {
                bindingResult.rejectValue("confirmNewPassword", "invalid.password.confirm", "새 비밀번호가 일치하지 않습니다.");
                model.addAttribute("modifyUserDTO", modifyUserDTO);
            } catch (IllegalArgumentException e) {
                bindingResult.rejectValue("newPassword", "invalid.newPassword", e.getMessage());
                model.addAttribute("modifyUserDTO", modifyUserDTO);
            } catch (NoSuchElementException e) {
                bindingResult.rejectValue("userId", "invalid.user", "사용자를 찾을 수 없습니다.");
                model.addAttribute("modifyUserDTO", modifyUserDTO);
            }
        }
        
        return "user/modify";
    }
    

    //=========================================================

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
            // 로그인된 사용자의 사용자 ID를 가져옴
            String userId = ((CustomUserDetails) principal).getUserId();
            // 사용자 ID로 사용자 정보를 가져옴
            UserDTO userDTO = userService.findByUserId(userId);
            // 사용자 정보를 모델에 추가
            model.addAttribute("userDTO", userDTO);
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        } else {
            System.out.println("CustomerDetails 객체가 아닙니다.");
            // 로그인되지 않은 사용자의 경우 로그인 페이지로 리다이렉트
            return "redirect:/user/login";
        }
    }

    //=========================================================

    /**
     * 회원탈퇴 프로세스
     * @param userDTO UserDTO 객체
     * @param bindingResult 유효성 검사 결과
     * @param redirectAttributes 리다이렉트 후 전달할 데이터
     * @param model
     * @param request
     * @return
     */
    
    @PostMapping("/delete_account")
    public String deleteAccount(@Valid @ModelAttribute("userDTO") UserDTO userDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) {
        
        // userId 필드에 오류가 있는지 확인합니다.
        boolean hasUserIdError = bindingResult.hasFieldErrors("userId");

        System.out.println("▶ userId: " + userDTO.getUserId());
        System.out.println("▶ userCode: " + userDTO.getUserCode());

        // userId 필드에 오류가 있다면
        if (hasUserIdError) {
            // userDTO를 모델에 추가
            model.addAttribute("userDTO", userDTO);
            System.err.println("회원탈퇴 실패: userId 필드에 오류가 있습니다.");
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        }
        
        // 유효성 검사(userId) 통과 시 회원탈퇴 진행
        try {
            // 회원정보를 삭제
            userService.deleteAccount(userDTO);
            // SecurityContextHolder의 인증 정보를 제거
            SecurityContextHolder.clearContext();
            // HttpSession 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                System.err.println("session is Null");
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
            System.err.println("회원탈퇴 실패: " + e.getMessage());
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 메시지를 bindingResult에 추가
            bindingResult.rejectValue("email", "delete.error", "회원 탈퇴 중 오류가 발생했습니다.");
            model.addAttribute("userDTO", userDTO);
            System.err.println("회원탈퇴 실패: " + e.getMessage());
            // 회원탈퇴 페이지로 이동
            return "user/delete_account";
        }
    }
}

