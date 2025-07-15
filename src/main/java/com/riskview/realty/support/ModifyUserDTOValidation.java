package com.riskview.realty.support;

import com.riskview.realty.domain.dto.ModifyUserDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ModifyUserDTO의 비밀번호 일치 검사를 위한 커스텀 어노테이션
 */
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
@jakarta.validation.Constraint(validatedBy = {ModifyUserDTOValidator.class})
public @interface ModifyUserDTOValidation {
    String message() default "비밀번호가 일치하지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends jakarta.validation.Payload>[] payload() default {};
}

class ModifyUserDTOValidator implements ConstraintValidator<ModifyUserDTOValidation, ModifyUserDTO> {
    @Override
    public void initialize(ModifyUserDTOValidation constraintAnnotation) {
        // 초기화 로직
    }

    @Override
    public boolean isValid(ModifyUserDTO value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        // 새 비밀번호가 입력된 경우에만 검증
        if (value.getNewPassword() != null && !value.getNewPassword().isEmpty()) {
            // 비밀번호 일치 검사
            if (!value.getNewPassword().equals(value.getConfirmNewPassword())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("비밀번호가 일치하지 않습니다.")
                       .addPropertyNode("confirmNewPassword")
                       .addConstraintViolation();
                return false;
            }

            // 비밀번호 길이 검사
            if (value.getNewPassword().length() < 8) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("비밀번호는 8자 이상이어야 합니다.")
                       .addPropertyNode("newPassword")
                       .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}