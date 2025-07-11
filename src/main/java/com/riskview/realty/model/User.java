package com.riskview.realty.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 저장하는 클래스
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@DynamicInsert
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_seq")
    private Long userSeq;

    /**
     * 사용자 고유 코드 (U + 8자리 숫자)
     */
    @Column(name = "user_code", unique = true, nullable = false, length = 20)
    private String userCode="U00000000"; // 기본값으로 임시 코드 설정

    /**
     * 사용자 고유 코드 생성
     */
    public void generateUserCode() {
        if (userCode != null && userSeq != null) {
            userCode = "U" + String.format("%08d", userSeq);
        }
    }

    @Column(name = "user_id", nullable = false, unique = true, length = 20)
    private String userId;

    @Column(name = "user_nickname", nullable = false, unique = true, length = 20)
    private String userNickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "role")
    private RolePermission role;

    @Column(name = "preferred_language", length = 20)
    private String preferredLanguage="ko";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private boolean isDeleted;
}