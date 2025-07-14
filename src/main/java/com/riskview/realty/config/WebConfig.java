package com.riskview.realty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정 클래스
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 정적 리소스 핸들러를 등록하는 메서드
     * @param registry 리소스 핸들러를 등록할 registry
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // CSS 리소스 핸들러 추가
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        
        // JavaScript 리소스 핸들러 추가
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
        // 이미지 리소스 핸들러 추가
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
