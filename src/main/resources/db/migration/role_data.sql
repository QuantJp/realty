-- Insert default roles and permissions
INSERT INTO role_permissions (role, can_upload, can_delete, can_manage_users, can_view_all_docs)
VALUES 
    ('user', true, false, false, false),
    ('admin', true, true, true, true);
