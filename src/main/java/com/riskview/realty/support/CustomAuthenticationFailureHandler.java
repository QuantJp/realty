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

    // UserRepository를 주입받아 사용자 정보를 조회
    @Autowired
    private UserRepository userRepository;

    // MessageSource를 주입받아 메시지를 가져옵니다.
    @Autowired
    private MessageSource messageSource;

    // 로그인 실패 시 호출되는 메서드
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 기본 오류 메시지
        String errorMessageKey = messageSource.getMessage("login.error.general", null, LocaleContextHolder.getLocale());
        String userId = request.getParameter("userId"); // 로그인 폼에서 넘어온 userId 파라미터를 가져옵니다.
        // 예외 유형에 따라 다른 메시지 설정
        if (exception instanceof UsernameNotFoundException) {
            // Spring Security가 사용자 ID를 찾지 못했을 때 (데이터베이스에 없는 사용자)
            errorMessageKey = messageSource.getMessage("login.error.user.notfound", null, LocaleContextHolder.getLocale());
        } else if (exception instanceof BadCredentialsException) {
            // 비밀번호가 일치하지 않을 때
            errorMessageKey = messageSource.getMessage("login.error.bad.credentials", null, LocaleContextHolder.getLocale());
        } else if (exception instanceof DisabledException) {
            // 사용자 계정이 비활성화되었을 때 (UserDetailsService에서 isEnabled=false로 설정 시)
            // is_deleted=1인 경우를 여기에 매핑할 수 있습니다.
            errorMessageKey = messageSource.getMessage("login.error.user.disabled", null, LocaleContextHolder.getLocale());
        }
        // 그 외 다른 AuthenticationException 유형에 대한 처리도 추가할 수 있습니다.

        // is_deleted=1인 사용자 분기 처리 (UsernameNotFoundException 전에 확인)
        // Spring Security의 UserDetailsService에서 이미 DisabledException을 발생시켰다면
        // 이 로직은 필요 없을 수 있습니다.
        // 하지만 사용자 정의 로직으로 직접 확인하려면 여기에 추가합니다.
        if (userId != null) {
            Optional<User> userOpt = userRepository.findByUserId(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.isDeleted()) {
                    errorMessageKey = messageSource.getMessage("login.error.user.deleted", null, LocaleContextHolder.getLocale());
                }
            }
        }

        String errorMessage = messageSource.getMessage(errorMessageKey, null, LocaleContextHolder.getLocale());

        // 오류 메시지를 URL 쿼리 파라미터로 인코딩하여 로그인 페이지로 리다이렉트
        String encodedErrorMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        // login.html에서 errorMsg 파라미터를 받도록 처리
        response.sendRedirect(request.getContextPath() + "/login?error=true&errorMsg=" + encodedErrorMessage);
    }
}