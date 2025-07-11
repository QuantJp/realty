package com.riskview.realty.service;

import com.riskview.realty.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import com.riskview.realty.domain.CustomUserDetails;
import com.riskview.realty.domain.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 데이터베이스에서 사용자 정보를 조회하여 SpringSecurity에서 사용할 수 있게끔 반환하는 클래스 
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("login.error.user.notfound"));

        if (user.isDeleted()) {
            throw new UsernameNotFoundException("login.error.user.deleted");
        }

        return new CustomUserDetails(
                user.getUserSeq(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getName(),
                user.getRole().getRole(),
                true,
                user.getRole().isCanManageUsers(),
                user.getRole().isCanViewAllDocs(),
                user.getUserCode()
        );
    }
}