package com.riskview.realty.service;

import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.domain.repository.RolePermissionRepository;
import com.riskview.realty.domain.repository.UserRepository;
import com.riskview.realty.model.RolePermission;
import com.riskview.realty.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${server.servlet.session.timeout}")
    private String sessionTimeout; // 세션 타임아웃 값을 주입받음

    /**
     * 회원가입
     * @param userDTO UserDTO 객체
     * @param verificationCode 인증코드
     * @param session 사용자 세션
     * @return 저장된 사용자 정보
     */
    public User registerUser(UserDTO userDTO, String verificationCode, HttpSession session) {
        // 세션에서 인증코드를 가져옴
        String sessionVerificationCode = (String) session.getAttribute("verificationCode");
        // 세션에 저장된 인증코드와 입력된 인증코드가 일치하지 않으면 예외 발생
        if (sessionVerificationCode == null || !sessionVerificationCode.equals(verificationCode)) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않습니다.");
        }

        /* 세션에 저장된 인증코드와 입력된 인증코드가 일치하면 사용자 정보 저장 */
        User user = new User(); // User 객체 생성
        user.setUserId(userDTO.getUserId()); // 사용자 ID 정보 저장
        user.setUserNickname(userDTO.getUserNickname()); // 사용자 닉네임 정보 저장
        user.setEmail(userDTO.getEmail()); // 사용자 이메일 정보 저장
        user.setName(userDTO.getName()); // 사용자 이름 정보 저장
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword())); // 사용자 비밀번호 정보 저장(해싱처리)

        // 기본 "user" 역할 조회 및 설정
        RolePermission userRole = rolePermissionRepository.findById("user")
                .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
        user.setRole(userRole);

        // 세션에서 인증코드 제거(회원가입이 완료되었으므로)
        session.removeAttribute("verificationCode");

        // 데이터베이스에 사용자 정보 저장
        User savedUser = userRepository.save(user);
        // 데이터베이스 저장 후 user_code 생성
        savedUser.generateUserCode();
        return userRepository.save(savedUser);
    }

    /**
     * 인증코드 전송
     * @param email 사용자 이메일
     * @param session 사용자 세션
     */
    public void sendVerificationCode(String email, HttpSession session) {
        // 인증코드 생성 메서드 호출
        String verificationCode = generateVerificationCode();
        // 인증 코드와 인증 코드를 수신할 이메일을 세션에 저장
        session.setAttribute("verificationCode", verificationCode);
        session.setAttribute("verificationCodeEmail", email);

        String subject = "Realty 회원가입 인증 코드"; // 이메일 제목
        String timeoutMinutes = sessionTimeout.replaceAll("[^0-9]", ""); // 숫자만 추출
        // 이메일 내용
        String message = String.format("""
            <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                <h2 style="color: #333;">Realty 회원가입 인증 코드</h2>
                <p>안녕하세요,</p>
                <p>Realty 회원가입을 위한 인증 코드입니다. 아래 코드를 회원가입 페이지에 입력해주세요.</p>
                <p style="font-size: 24px; font-weight: bold; color: #007bff; background-color: #f0f8ff; padding: 10px; border-radius: 5px; display: inline-block;">
                    %s
                </p>
                <p>본 코드는 %s분 동안 유효합니다. 만약 본인이 요청하지 않았다면 이 이메일을 무시해주세요.</p>
                <p>감사합니다.<br/>Realty 팀 드림</p>
            </div>
            """, verificationCode, timeoutMinutes); // %s에 해당하는 변수들을 순서대로 전달

        // 이메일 전송
        emailService.sendEmail(email, subject, message);
    }

    /**
     * 인증코드 생성 로직(난수)
     * @return 생성된 인증코드
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int length = 6; // 인증코드의 길이는 6
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 인증코드는 대문자와 숫자로 구성
        // 6자리 인증 코드를 생성하기 위해 6번 반복하여 문자를 추가하므로 StringBuilder가 유리함
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            // chracters.length(): 여기서는 36를 반환
            // random.nextInt(characters.length()): 여기서는 0~35 사이의 난수를 반환
            // characters.charAt(random.nextInt(characters.length())): 여기서는 0~35 사이의 난수를 인덱스로 사용하여 문자를 반환
            // code.append(characters.charAt(random.nextInt(characters.length()))): 여기서는 0~35 사이의 난수를 인덱스로 사용하여 문자를 반환한 후 StringBuilder에 추가
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        // StringBuilder 객체를 문자열로 변환하여 반환
        return code.toString();
    }
}
