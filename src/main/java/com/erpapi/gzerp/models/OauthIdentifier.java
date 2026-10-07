package com.erpapi.gzerp.models;


import com.erpapi.gzerp.enums.Marketplace;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "OauthIdentifier" )
public class OauthIdentifier {

    @Id
    @Column(name = "id")
    private UUID state;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "marketplace")
    @Enumerated(EnumType.STRING)
    private Marketplace marketplace;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public OauthIdentifier() {
    }

    public UUID getState() {
        return state;
    }

    public void setState(UUID state) {
        this.state = state;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
