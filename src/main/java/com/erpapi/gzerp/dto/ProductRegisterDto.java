package com.erpapi.gzerp.dto;


import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductRegisterDto {

    @NotBlank(message = "Invalid product name, please enter a valid one")
    @Size(max = 255)
    private String name;

    @PositiveOrZero
    @NotNull
    private int stock;

    @Digits(integer = 10 , fraction = 2)
    @NotNull
    @Positive
    private BigDecimal baseSellPrice;

    @Digits(integer = 10, fraction = 2)
    @NotNull
    @PositiveOrZero
    private BigDecimal costPrice;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 50)
    private String sku;

    public ProductRegisterDto() {
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
