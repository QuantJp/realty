package com.riskview.realty.support;

import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.lang.NonNull;

// UserDTO 클래스 또는 그 자식 클래스만 지원하는 Validator
@Component
public class UserDTOValidator implements Validator {

    @Autowired
    private UserRepository userRepository;

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

        // 비밀번호 일치 확인
        if (!userDTO.getPassword().equals(userDTO.getPasswordConfirm())) {
            // 비밀번호와 비밀번호 확인이 일치하지 않을 때 error.properties에 저장된 오류 메시지 등록
            errors.rejectValue("passwordConfirm", "validation.password.mismatch");
        }

        // 이메일 중복 확인
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            // 이메일이 중복될 때 error.properties에 저장된 오류 메시지 등록
            errors.rejectValue("email", "validation.email.duplicate");
        }
    }
}
