package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.Marketplace;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "marketplace_tokens")
public class MarketplaceTokens {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenants tenant;

    @Enumerated(EnumType.STRING)
    private Marketplace marketplace;


    private Long ShopId;
    private String AccessToken;
    private String RefreshToken;
    private LocalDateTime AccesDateExpiresAt;

    public MarketplaceTokens() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenants getTenant() {
        return tenant;
    }

    public void setTenant(Tenants tenant) {
        this.tenant = tenant;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public Long getShopId() {
        return ShopId;
    }

    public void setShopId(Long shopId) {
        ShopId = shopId;
    }

    public String getAccessToken() {
        return AccessToken;
    }

    public void setAccessToken(String accessToken) {
        AccessToken = accessToken;
    }

    public String getRefreshToken() {
        return RefreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        RefreshToken = refreshToken;
    }

    public LocalDateTime getAccesDateExpiresAt() {
        return AccesDateExpiresAt;
    }

    public void setAccesDateExpiresAt(LocalDateTime accesDateExpiresAt) {
        AccesDateExpiresAt = accesDateExpiresAt;
    }
}
