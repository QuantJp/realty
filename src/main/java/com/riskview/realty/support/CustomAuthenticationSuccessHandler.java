package com.riskview.realty.support;

import com.riskview.realty.domain.CustomUserDetails;
import com.riskview.realty.domain.dto.UserDTO;
import com.riskview.realty.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     Authentication authentication) throws IOException, ServletException {
        
        // 로그인 성공 시 세션에 사용자 정보 저장
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUserId();
            
            // UserDTO 객체 생성 및 세션에 저장
            UserDTO userDTO = userService.findByUserId(userId);
            HttpSession session = request.getSession();
            session.setAttribute("user", userDTO);
            
            System.out.println("로그인 성공: 사용자 " + userId + "의 세션 정보가 저장되었습니다.");
        }
        
        // 기본 성공 URL로 리다이렉트
        response.sendRedirect("/");
    }
} 