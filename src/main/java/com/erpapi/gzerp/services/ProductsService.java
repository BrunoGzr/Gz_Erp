package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.*;
import com.erpapi.gzerp.exceptions.InvalidProductException;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.MarketplaceListingRepo;
import com.erpapi.gzerp.repositories.ProductsRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import com.erpapi.gzerp.security.TenantContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductsService {

    private final ProductsRepo productsRepo;
    private final TenantsRepo tenantsRepo;
    private final MarketplaceListingRepo listingRepo;

    public ProductsService(ProductsRepo productsRepo, TenantsRepo tenantsRepo, MarketplaceListingRepo marketplaceListingRepo) {
        this.productsRepo = productsRepo;
        this.tenantsRepo = tenantsRepo;
        this.listingRepo = marketplaceListingRepo;
    }

    public ProductResponseDto registerProduct(@Valid ProductRegisterDto dto, UsersAccounts user) {
        Long tenantId = TenantContext.required();
        this.verifyUse(dto, tenantId);
        Products newProduct = new Products();
        newProduct.setTenant(tenantsRepo.findById(tenantId).orElseThrow());
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
        if (dto.getSku() == null || dto.getSku().isBlank()){
            throw new InvalidProductException("Invalid / blank Sku, please use another one");
        }
        if (productsRepo.existsBySkuAndTenantId(dto.getSku(), tenantId)){
            throw new InvalidProductException("Product Sku already in use, please use another one.");
        }
        if (productsRepo.existsByNameAndTenantId(dto.getName(),tenantId)){
            throw  new InvalidProductException("Product Name already in use, please use another one.");
        }
    }

//    public PageResponseDto productPagesGet(Pageable pageable){
//        Long tenantId = TenantContext.required();
//        Page<Products> page = productsRepo.findByTenantIdAndActiveTrue(tenantId,pageable);
//        PageResponseDto pageResponseDto = new PageResponseDto();
//        pageResponseDto.setItens(page.getContent().stream().map(products -> toResponse(products)).toList());
//        pageResponseDto.setActualPage(page.getNumber());
//        pageResponseDto.setTotalPage(page.getTotalPages()); // Quantas páginas existem
//        pageResponseDto.setTotalItens(page.getTotalElements()); // Quantos itens ao todo no banco
//        pageResponseDto.setPageSize(page.getSize()); // Tamanho da pagina atual, Bagulho confuso q só a porra
//        pageResponseDto.setHasNext(page.hasNext());
//
//
//
//
//
//        return pageResponseDto;
//    }

    public ProductPageResponseDto getProductsPage(Pageable pageable) {

        Long tenantId = TenantContext.required();
        Page<Products> page = productsRepo.findByTenantIdAndActiveTrue(tenantId,pageable);


        if (page.isEmpty()){
            return new ProductPageResponseDto(
                    new ArrayList<>(),
                    page.getNumber(),
                    page.getTotalPages(),
                    page.getTotalElements(),
                    page.getSize(),
                    false
            );

        }

        List<Long> productsIds = page.getContent().stream().map(Products::getId).toList();

        List<ListingCountProjectionDto> rawCounts = listingRepo.countListingsByProductIds(productsIds);

        Map<Long, Map<String, Long>> countsByProductId = new HashMap<>(); // Formato { 1: {{Shopee: 1}, {Mercado_livre: 2}}

        for (ListingCountProjectionDto row : rawCounts) {
            countsByProductId.computeIfAbsent(row.getProductId(), k -> new HashMap<>())
                    .put(row.getMarketplace(), row.getTotal());
            }

        List<ProductResponseDto> dtos = page.getContent().stream()
                .map(product -> {
                    Map<String, Long> productsCounts = countsByProductId.getOrDefault(product.getId(), new HashMap<>());
                    ProductResponseDto dto = new ProductResponseDto();
                    dto.setPublicId(product.getPublicId());
                    dto.setSku(product.getSku());
                    dto.setName(product.getName());
                    dto.setStock(product.getStock());
                    dto.setBaseSellPrice(product.getBaseSellPrice());
                    dto.setCostPrice(product.getCostPrice());
                    dto.setImageUrl(product.getImageUrl());
                    dto.setSales(product.getSales());
                    dto.setCreatedAt(product.getCreatedAt());
                    dto.setUpdatedAt(product.getUpdatedAt());
                    dto.setListingCount(productsCounts);
                    return  dto;
                }).toList();

        return new ProductPageResponseDto(
                dtos,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext()
        );














    }


}