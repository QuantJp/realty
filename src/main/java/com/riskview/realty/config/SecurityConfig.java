package com.riskview.realty.config;

import com.riskview.realty.service.CustomUserDetailsService;
import com.riskview.realty.support.CustomAuthenticationFailureHandler;
import com.riskview.realty.support.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Spring 설정 클래스라는 의미임
@EnableWebSecurity // SpringSecurity 웹 보안 설정 활성화
public class SecurityConfig {

    // 사용자 인증 관련 서비스를 제공하는 클래스
    private final CustomUserDetailsService customUserDetailsService;

    // 보안 설정에서 사용자 인증을 처리하기 위해 생성자 주입
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    // 로그인 여부 및 권한에 따라 URL 접근 제어
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .userDetailsService(customUserDetailsService) // 사용자 권한, 인증 관련 서비스를 제공
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/user/login", 
                                "/user/register", 
                                "/user/verify-email-code", 
                                "/user/send-verification-code", 
                                "/user/register_success", 
                                "/user/delete_account", 
                                "/user/modify", 
                                "/favicon.ico", 
                                "/css/**", 
                                "/js/**", 
                                "/images/**",
                                "/board/**",
                                "/static/**")
                                .permitAll() // 모든 사람이 접근 가능
                .requestMatchers("/admin/**").hasRole("ADMIN") // ADMIN 역할만 접근 가능
                .anyRequest().authenticated() // 나머지는 전부 로그인한 사람만 접근 가능
            )
            .formLogin(form -> form
                .loginPage("/user/login") // 로그인 페이지
                .loginProcessingUrl("/user/login") // 로그인 처리 URL
                .usernameParameter("userId") // 로그인 폼에서 사용할 파라미터 이름
                .defaultSuccessUrl("/", true) // 로그인 성공 시 이동할 URL
                .successHandler(customAuthenticationSuccessHandler) // 로그인 성공 시 처리할 핸들러
                .failureHandler(customAuthenticationFailureHandler) // 로그인 실패 시 처리할 핸들러
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/user/login?logout") // 로그아웃 성공 시 이동할 URL
                .invalidateHttpSession(true) // 세션 무효화
                .deleteCookies("JSESSIONID") // 쿠키 삭제
                .permitAll())
            .sessionManagement(session -> session
                .invalidSessionUrl("/user/login?expired") // 세션 만료 시 이동할 URL
                .maximumSessions(1) // 동시 로그인 허용 개수
                .maxSessionsPreventsLogin(false)
            );
        return http.build();
    }

        // 비밀번호 암호화를 위한 빈
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호 해싱에 사용됨
        return new BCryptPasswordEncoder();
    }
}
