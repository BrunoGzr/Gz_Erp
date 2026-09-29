package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Products;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProductResponseDto {

    private UUID publicId;

    private String sku;

    private String name;

    private int stock;

    private BigDecimal baseSellPrice;

    private BigDecimal costPrice;

    private String imageUrl;

    private int sales;

    private LocalDateTime createdAt;

    public ProductResponseDto(Products product) {
        this.name = product.getName();
        this.publicId = product.getPublicId();
        this.sku = product.getSku();
        this.stock = product.getStock();
        this.baseSellPrice = product.getBaseSellPrice();
        this.costPrice = product.getCostPrice();
        this.imageUrl = product.getImageUrl();
        this.createdAt = product.getCreatedAt();
        this.sales = product.getSales();
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public BigDecimal getBaseSellPrice() {
        return baseSellPrice;
    }

    public void setBaseSellPrice(BigDecimal baseSellPrice) {
        this.baseSellPrice = baseSellPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getSales() {
        return sales;
    }

    public void setSales(int sales) {
        this.sales = sales;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
