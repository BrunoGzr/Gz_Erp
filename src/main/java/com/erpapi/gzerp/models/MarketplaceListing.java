package com.erpapi.gzerp.models;

import com.erpapi.gzerp.enums.Marketplace;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "marketplace_listing")
public class MarketplaceListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "public_id")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID publicId;

    @Column(name = "external_listing_id")
    private String externalListingId;

    @Column(name = "title", length = 256)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace", nullable = false)
    private Marketplace marketplace;

    @Column(name = "stock", nullable = false)
    private int stock = 0 ;

    @Column(name = "sales", nullable = false)
    private int sales = 0 ;

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "marketplace_token_id", updatable = false, nullable = false)
    private MarketplaceTokens account;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id_intern", updatable = false, nullable = false)
    private Products product;

    public MarketplaceListing() {
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public String getExternalListingId() {
        return externalListingId;
    }

    public void setExternalListingId(String externalListingId) {
        this.externalListingId = externalListingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Marketplace getMarketplace() {
        return marketplace;
    }

    public void setMarketplace(Marketplace marketplace) {
        this.marketplace = marketplace;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Products getProduct() {
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public MarketplaceTokens getAccount() {
        return account;
    }

    public void setAccount(MarketplaceTokens account) {
        this.account = account;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
