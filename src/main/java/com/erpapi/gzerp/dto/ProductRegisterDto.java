package com.erpapi.gzerp.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductRegisterDto {

    @NotBlank(message = "Invalid product name, please enter a valid one")
    @Size(max = 256)
    private String name;

    @PositiveOrZero
    @NotNull
    private Integer stock;

    @Digits(integer = 10 , fraction = 2)
    @NotNull
    @Positive
    private BigDecimal baseSellPrice;

    @Digits(integer = 10, fraction = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal costPrice;

    @Size(max = 500)
    @URL
    @Nullable
    private String imageUrl;

    @Size(max = 50)
    private String sku;

    @PositiveOrZero
    private Integer minStock = 20;

    @NotNull
    private List<UUID> categoryIds;

    public ProductRegisterDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStock() {
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public List<UUID> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<UUID> categoryIds) {
        this.categoryIds = categoryIds;
    }
}
