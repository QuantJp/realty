package com.riskview.realty.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 사용자 인증 정보를 저장하는 클래스
 * Getter만 구현하여 생성된 이후에 값이 변경되는 걸 방지
 */
public class CustomUserDetails implements UserDetails {

    private Long userSeq; // 사용자 고유 식별자
    private String userId; // 사용자 ID
    private String email; // 사용자 이메일
    private String passwordHash; // 사용자 비밀번호
    private String name; // 사용자 이름
    private String role; // 사용자 권한
    private boolean enabled; // 사용자 활성화 상태
    private boolean canManageUsers; // 사용자 관리 권한
    private boolean canViewAllDocs; // 모든 문서 조회 권한
    private String userCode; // 사용자 코드

    // 인터페이스를 구현하는 클래스므로 수동으로 생성자 주입
    public CustomUserDetails(Long userSeq, String userId,String email, String passwordHash, String name, String role, boolean enabled, boolean canManageUsers, boolean canViewAllDocs, String userCode) {
        this.userSeq = userSeq; // userSeq는 데이터베이스에서 자동 생성되므로 null로 초기화
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.enabled = enabled;
        this.canManageUsers = canManageUsers;
        this.canViewAllDocs = canViewAllDocs;
        this.userCode = userCode;
    }

    public Long getUserSeq() {
        return userSeq; // 사용자 고유 식별자를 문자열로 반환
    }

    // 사용자 ID 반환
    public String getUserId() {
        return userId;
    }

    // 사용자 이름 반환
    public String getName() {
        return name;
    }

    // 권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한을 ROLE_으로 시작하는 문자열로 변환(예: USER -> ROLE_USER)
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    // 패스워드 반환
    @Override
    public String getPassword() {
        return passwordHash;
    }

    // 사용자 이름 반환
    @Override
    public String getUsername() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // 계정이 만료되지 않았는지 확인
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정이 잠겨있지 않았는지 확인
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호가 만료되지 않았는지 확인
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정이 활성화되었는지 확인
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // 사용자 관리 권한 반환
    public boolean getCanManageUsers() {
        return canManageUsers;
    }

    // 모든 문서 조회 권한 반환
    public boolean getCanViewAllDocs() {
        return canViewAllDocs;
    }
    
    // 사용자 코드 반환
    public String getUserCode() {
        return userCode;
    }
}
