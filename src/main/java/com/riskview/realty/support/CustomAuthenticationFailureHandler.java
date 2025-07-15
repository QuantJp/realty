package com.riskview.realty.support;

import com.riskview.realty.domain.repository.UserRepository;
import com.riskview.realty.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource; // comments above each line of code // MessageSource를 임포트합니다.
import org.springframework.context.i18n.LocaleContextHolder; // comments above each line of code // 현재 로케일을 가져오기 위해 임포트합니다.
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

// 로그인 실패를 처리하는 커스텀 핸들러
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    /**
     * 로그인 실패 시 호출되는 메서드
     * @param request
     * @param response
     * @param exception
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        System.out.println("[DEBUG] onAuthenticationFailure 호출됨");
        System.out.println("[DEBUG] 예외 클래스: " + exception.getClass().getName());
        System.out.println("[DEBUG] 예외 메시지: " + exception.getMessage());

        /**
         * 로그인 실패 시 기본 오류 메시지 설정
         */
        // 기본 오류 메시지
        String errorMessageKey = messageSource.getMessage("login.error.general", null, LocaleContextHolder.getLocale());
        // 사용자 요청에서 넘어온 userId 파라미터를 가져옴
        String userId = request.getParameter("userId");

        /**
         * 사용자 삭제 여부를 데이터베이스에서 확인
         * is_deleted=1인 사용자 분기 처리 (UsernameNotFoundException 전에 확인)
         */
        if (userId != null) {
            System.out.println("[DEBUG] userId로 사용자 정보 조회");
            Optional<User> userOpt = userRepository.findByUserId(userId);
            // 사용자 정보가 존재할 때
            if (userOpt.isPresent() && userOpt.get().isDeleted()) {
                System.out.println("[DEBUG] 사용자가 삭제됨");
                errorMessageKey = "login.error.user.deleted";
            // Spring Security가 사용자 ID를 찾지 못했을 때 (데이터베이스에 없는 사용자)
            } else if (exception instanceof UsernameNotFoundException) {
                errorMessageKey = "login.error.user.notfound";
            // 비밀번호가 일치하지 않을 때
            } else if (exception instanceof BadCredentialsException) {
                errorMessageKey = "login.error.bad.credentials";
                // 사용자 계정이 비활성화되었을 때 (UserDetailsService에서 isEnabled=false로 설정 시)
                // is_deleted=1인 경우를 여기에 매핑할 수 있습니다.
            } else if (exception instanceof DisabledException) {
                errorMessageKey = "login.error.user.disabled";
            }
        }

        /**
         * 오류 메시지 다국어 지원 및 URL 인코딩 처리
         */
        // 다국어 오류 메시지 가져오기
        String errorMessage = messageSource.getMessage(errorMessageKey, null, LocaleContextHolder.getLocale());
        // 오류 메시지 URL 인코딩
        String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        // 로그인 페이지로 리다이렉트(URL에 오류 메시지 포함)
        response.sendRedirect(request.getContextPath() + "/login?error=true&errorMsg=" + encodedErrorMessage);
    }
}