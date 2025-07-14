package com.riskview.realty.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 컨트롤러
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * 관리자 페이지
     * PreAuthorize : 메서드 실행 전에 권한 체크
     * hasRole('ADMIN') : ADMIN 권한을 가진 사용자만 접근 가능
     * principal : 현재 로그인한 사용자 정보
     * @return 관리자 페이지
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // 
    public String adminPage() {
        return "admin/admin";
    }

    /**
     * 사용자 관리 페이지
     * PreAuthorize : 메서드 실행 전에 권한 체크
     * hasRole('ADMIN') : ADMIN 권한을 가진 사용자만 접근 가능
     * principal : 현재 로그인한 사용자 정보
     * @return 사용자 관리 페이지
     */
    @GetMapping("/manage-users")
    @PreAuthorize("hasRole('ADMIN') and principal.canManageUsers")
    public String manageUsersPage() {
        return "admin/manage_users";
    }
    
    /**
     * 모든 문서 조회 페이지
     * PreAuthorize : 메서드 실행 전에 권한 체크
     * hasRole('ADMIN') : ADMIN 권한을 가진 사용자만 접근 가능
     * principal : 현재 로그인한 사용자 정보
     * @return 모든 문서 조회 페이지
     */
    @GetMapping("/view-all-docs")
    @PreAuthorize("hasRole('ADMIN') and principal.canViewAllDocs")
    public String viewAllDocsPage() {
        return "admin/view_all_docs";
    }
}
