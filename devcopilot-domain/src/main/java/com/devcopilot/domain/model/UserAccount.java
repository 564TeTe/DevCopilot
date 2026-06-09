package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.ResourceStatus;
import com.devcopilot.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "sys_user")
public class UserAccount extends BaseEntity {

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 64)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResourceStatus status;

    protected UserAccount() {
    }

    public UserAccount(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = UserRole.USER;
        this.status = ResourceStatus.ACTIVE;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public ResourceStatus getStatus() {
        return status;
    }
}
