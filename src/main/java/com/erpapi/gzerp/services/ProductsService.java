package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.PageResponseDto;
import com.erpapi.gzerp.dto.ProductRegisterDto;
import com.erpapi.gzerp.dto.ProductResponseDto;
import com.erpapi.gzerp.exceptions.InvalidProductException;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.ProductsRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import com.erpapi.gzerp.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductsService {

    private final ProductsRepo productsRepo;
    private final TenantsRepo tenantsRepo;

    public ProductsService(ProductsRepo productsRepo, TenantsRepo tenantsRepo) {
        this.productsRepo = productsRepo;
        this.tenantsRepo = tenantsRepo;
    }

    public ProductResponseDto registerProduct(@Valid ProductRegisterDto dto, UsersAccounts user) {
        Long tenantId = TenantContext.required();
        this.verifyUse(dto, tenantId);
        Products newProduct = new Products();
        newProduct.setTenant(tenantsRepo.findById(tenantId).orElseThrow());
        newProduct.setUser(user);
        newProduct.setBaseSellPrice(dto.getBaseSellPrice());
        newProduct.setImageUrl(dto.getImageUrl());
        newProduct.setActive(true);
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

    public void verifyUse(ProductRegisterDto dto, Long tenantId){
        if (productsRepo.existsBySkuAndTenantId(dto.getSku(), tenantId)){
            throw new InvalidProductException("Product Sku already in use, please use another one.");
        }
        if (productsRepo.existsByNameAndTenantId(dto.getName(),tenantId)){
            throw  new InvalidProductException("Product Name already in use, please use another one.");
        }
    }

    public PageResponseDto productPagesGet(Pageable pageable){
        Long tenantId = TenantContext.required();
        Page<Products> page = productsRepo.findByTenantIdAndActiveTrue(tenantId,pageable);
        PageResponseDto pageResponseDto = new PageResponseDto();
        pageResponseDto.setItens(page.getContent().stream().map(products -> toResponse(products)).toList());
        pageResponseDto.setActualPage(page.getNumber());
        pageResponseDto.setTotalPage(page.getTotalPages()); // Quantas páginas existem
        pageResponseDto.setTotalItens(page.getTotalElements()); // Quantos itens ao todo no banco
        pageResponseDto.setPageSize(page.getSize()); // Tamanho da pagina atual, Bagulho confuso q só a porra
        pageResponseDto.setHasNext(page.hasNext());

        return pageResponseDto;
    }

    private ProductResponseDto toResponse(Products product) {
        return new ProductResponseDto(product);
    }


}
