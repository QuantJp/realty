package com.riskview.realty.service;

import com.riskview.realty.domain.dto.ModifyUserDTO;
import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.model.User;
import com.riskview.realty.domain.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModifyUserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 수정
     * @param modifyUserDTO 수정할 사용자 정보
     * @throws NoSuchElementException 사용자를 찾을 수 없을 때 발생
     * @throws InvalidPasswordException 현재 비밀번호가 일치하지 않을 때 발생
     * @throws PasswordMismatchException 새 비밀번호가 일치하지 않을 때 발생
     */
    @Transactional
    public void modifyUserInfo(ModifyUserDTO modifyUserDTO, HttpSession session) {
        // 비밀번호 검증
        if (!validateCurrentPassword(modifyUserDTO.getUserId(), modifyUserDTO.getCurrentPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 사용자 조회
        User existingUser = userRepository.findByUserId(modifyUserDTO.getUserId())
            .orElseThrow(() -> new NoSuchElementException("해당 사용자를 찾을 수 없습니다: " + modifyUserDTO.getUserId()));

        // 비밀번호 변경 (새 비밀번호가 입력된 경우에만)
        if (modifyUserDTO.getNewPassword() != null && !modifyUserDTO.getNewPassword().isEmpty()) {
            if (!modifyUserDTO.getNewPassword().equals(modifyUserDTO.getConfirmNewPassword())) {
                throw new PasswordMismatchException("새 비밀번호가 일치하지 않습니다.");
            }
            // 새 비밀번호가 현재 비밀번호와 다른지 확인
            if (passwordEncoder.matches(modifyUserDTO.getNewPassword(), existingUser.getPasswordHash())) {
                throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
            }
            existingUser.setPasswordHash(passwordEncoder.encode(modifyUserDTO.getNewPassword()));
        }

        // 기본 정보 업데이트
        existingUser.setName(modifyUserDTO.getName());
        existingUser.setUserNickname(modifyUserDTO.getUserNickname());
        existingUser.setEmail(modifyUserDTO.getEmail());
        
        // 변경사항 저장
        userRepository.save(existingUser);

        // 세션에 저장된 사용자 정보 업데이트
        UserDTO userDTO = (UserDTO) session.getAttribute("user");
        if (userDTO != null) {
            // 기존 userDTO 객체의 속성만 업데이트
            userDTO.setUserId(existingUser.getUserId());
            userDTO.setUserNickname(existingUser.getUserNickname());
            userDTO.setName(existingUser.getName());
            userDTO.setEmail(existingUser.getEmail());

            session.setAttribute("user", userDTO);
            session.setAttribute("successMessage", "회원정보가 수정되었습니다.");
            System.out.println("회원정보가 수정되었습니다.");
        } else {
            throw new IllegalStateException("세션에 저장된 사용자 정보가 없습니다.");
        }
    }

    /**
     * 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보 DTO
     * @throws NoSuchElementException 사용자를 찾을 수 없을 때 발생
     */
    public UserDTO getUserInfo(String userId) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("다음 사용자 ID의 사용자를 찾을 수 없습니다: " + userId));

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setName(user.getName());
        userDTO.setUserNickname(user.getUserNickname());
        userDTO.setEmail(user.getEmail());

        return userDTO;
    }
    
    /**
     * 현재 비밀번호 검증
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호
     * @return 비밀번호가 일치하는지 여부
     */
    private boolean validateCurrentPassword(String userId, String currentPassword) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("다음 사용자 ID의 사용자를 찾을 수 없습니다: " + userId));
        
        return passwordEncoder.matches(currentPassword, user.getPasswordHash());
    }

    /**
     * 현재 비밀번호가 일치하지 않을 때 발생하는 예외
     */
    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
            System.out.println("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 새 비밀번호가 일치하지 않을 때 발생하는 예외
     */
    public static class PasswordMismatchException extends RuntimeException {
        public PasswordMismatchException(String message) {
            super(message);
            System.out.println("새 비밀번호가 일치하지 않습니다.");
        }
    }
}