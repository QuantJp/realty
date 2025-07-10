package com.riskview.realty.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 관리자 컨트롤러(ROLE_ADMIN)
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPage() {
        return "admin/admin";
    }

    @GetMapping("/manage-users")
    @PreAuthorize("hasRole('ADMIN') and principal.canManageUsers")
    public String manageUsersPage() {
        return "admin/manage_users";
    }

    @GetMapping("/view-all-docs")
    @PreAuthorize("hasRole('ADMIN') and principal.canViewAllDocs")
    public String viewAllDocsPage() {
        return "admin/view_all_docs";
    }
}
