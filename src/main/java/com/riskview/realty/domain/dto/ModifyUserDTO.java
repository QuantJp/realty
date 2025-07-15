package com.riskview.realty.domain.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.riskview.realty.support.ModifyUserDTOValidation;

/**
 * 회원정보 수정을 위한 DTO
 */
@Getter
@Setter
@ModifyUserDTOValidation
public class ModifyUserDTO {
    // 읽기 전용 필드
    private String userId; // 사용자 ID

    @NotBlank(message = "{validation.name.notblank}")
    @Size(min = 2, max = 30, message = "{validation.name.size}")
    private String name; // 사용자 이름

    @NotBlank(message = "{validation.userNickname.notblank}")
    @Size(min = 2, max = 20, message = "{validation.userNickname.size}")
    private String userNickname; // 사용자 닉네임

    // 읽기 전용 필드
    private String email; // 사용자 이메일

    // 비밀번호 변경 관련 필드
    @NotBlank(message = "{validation.currentPassword.notblank}")
    private String currentPassword; // 현재 비밀번호

    // 새 비밀번호는 선택사항 (변경하지 않을 수도 있음)
    private String newPassword; // 새 비밀번호

    // 새 비밀번호 확인은 선택사항
    private String confirmNewPassword; // 비밀번호 확인

    // 사용자 코드 (선택사항)
    private String userCode; // 사용자 코드
}