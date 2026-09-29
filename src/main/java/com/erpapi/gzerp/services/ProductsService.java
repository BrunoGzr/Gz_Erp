package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.ProductRegisterDto;
import com.erpapi.gzerp.dto.ProductResponseDto;
import com.erpapi.gzerp.enums.Permissions;
import com.erpapi.gzerp.exceptions.InvalidProductException;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.repositories.ProductsRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import com.erpapi.gzerp.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class ProductsService {

    private final ProductsRepo productsRepo;
    private final TenantsRepo tenantsRepo;

    public ProductsService(ProductsRepo productsRepo, TenantsRepo tenantsRepo) {
        this.productsRepo = productsRepo;
        this.tenantsRepo = tenantsRepo;
    }


    public ProductResponseDto registerProduct(@Valid ProductRegisterDto dto) {
        this.verifyUse(dto);
        Products newProduct = new Products();
        long tenantId = TenantContext.required();
        newProduct.setTenant(tenantsRepo.findById(tenantId).orElseThrow());
        newProduct.setBaseSellPrice(dto.getBaseSellPrice());
        newProduct.setImageUrl(dto.getImageUrl());
        newProduct.setPublicId(UUID.randomUUID());
        newProduct.setCostPrice(dto.getCostPrice());
        newProduct.setName(dto.getName());
        newProduct.setStock(dto.getStock());
        if (dto.getSku() == null || dto.getSku().isBlank()) {
            return new ProductResponseDto(productsRepo.save(newProduct));
        }
        newProduct.setSku(dto.getSku());
        return new ProductResponseDto(productsRepo.save(newProduct));
    }



    public void verifyUse(ProductRegisterDto dto){
        if (productsRepo.existsBySku(dto.getSku())){
            throw new InvalidProductException("Product Sku already in use, please use another one.");
        }
        if (productsRepo.existsByName(dto.getName())){
            throw  new InvalidProductException("Product Name already in use, please use another one.");
        }
    }
}
