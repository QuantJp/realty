package com.riskview.realty.domain.repository;

import com.riskview.realty.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/* 사용자 ID를 기준으로 User 객체에서 조회 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional 객체를 사용해 NullPointException 방지
    Optional<User> findByUserId(String userId); // 사용자 ID로 사용자 조회
    Optional<User> findByEmail(String email); // 사용자 이메일로 사용자 조회
    Optional<User> findByUserCode(String userCode); // 사용자 코드로 사용자 조회
    
}