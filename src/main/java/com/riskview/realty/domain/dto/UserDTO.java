package com.riskview.realty.domain.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 사용자 정보를 저장하는 DTO
 */
@Getter
@Setter
public class UserDTO {
    @NotBlank(message = "{validation.name.size}")
    @Size(min = 2, max = 30, message = "{validation.name.size}")
    private String name; // 사용자 이름

    @NotBlank(message = "{validation.userId.size}")
    @Size(min = 4, max = 20, message = "{validation.userId.size}")
    private String userId; // 사용자 ID

    @NotBlank(message = "{validation.userNickname.size}")
    @Size(min = 2, max = 20, message = "{validation.userNickname.size}")
    private String userNickname; // 사용자 닉네임

    @NotBlank(message = "{validation.password.size}")
    @Size(min = 8, message = "{validation.password.size}")
    private String password; // 사용자 비밀번호

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm; // 사용자 비밀번호 확인

    @NotBlank(message = "{validation.email.invalid}")
    @Email(message = "{validation.email.invalid}")
    private String email; // 사용자 이메일

    @NotBlank(message = "인증코드를 입력해주세요.")
    private String verificationCode; // 사용자 인증코드

    private String userCode; // 사용자 코드
}
