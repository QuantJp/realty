package com.riskview.realty.support;

import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.domain.repository.UserRepository;
import com.riskview.realty.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.lang.NonNull;

// UserDTO 클래스 또는 그 자식 클래스만 지원하는 Validator
@Component
public class UserDTOValidator implements Validator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // UserDTO 클래스 또는 그 자식 클래스만 지원
    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserDTO.class.isAssignableFrom(clazz);
    }

    /**
     * UserDTO 객체의 유효성 검사
     * @param target Object 객체(Validator 인터페이스의 validate 메서드는 Object 타입의 파라미터를 받으므로)
     * @param errors 유효성 검사 결과 오류 메시지
     */
    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        UserDTO userDTO = (UserDTO) target;

        // 사용자 ID 존재 확인
        User user = userRepository.findByUserId(userDTO.getUserId()).orElse(null);
        if (user == null) {
            errors.rejectValue("userId", "login.error.user.notfound");
            return;
        }

        // 탈퇴한 사용자 확인
        if (user.isDeleted()) {
            errors.rejectValue("userId", "login.error.user.deleted");
            return;
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPasswordHash())) {
            errors.rejectValue("password", "login.error.bad.credentials");
        }

        // 비밀번호 일치 확인
        if (!userDTO.getPassword().equals(userDTO.getPasswordConfirm())) {
            errors.rejectValue("passwordConfirm", "validation.password.mismatch");
        }

        // 이메일 중복 확인
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            errors.rejectValue("email", "validation.email.duplicate");
        }
    }
}
