package com.riskview.realty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
public class RolePermission {

    @Id
    @Column(name = "role", length = 50)
    private String role;

    @Column(name = "can_upload")
    private boolean canUpload;

    @Column(name = "can_delete")
    private boolean canDelete;

    @Column(name = "can_manage_users")
    private boolean canManageUsers;

    @Column(name = "can_view_all_docs")
    private boolean canViewAllDocs;
}