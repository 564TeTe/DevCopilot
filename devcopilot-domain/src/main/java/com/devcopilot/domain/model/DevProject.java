package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.ResourceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "dev_project")
public class DevProject extends BaseEntity {

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 1024)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResourceStatus status;

    protected DevProject() {
    }

    public DevProject(Long ownerId, String name, String description) {
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.status = ResourceStatus.ACTIVE;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ResourceStatus getStatus() {
        return status;
    }
}
