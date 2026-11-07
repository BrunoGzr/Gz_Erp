package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.*;
import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.enums.StockFilter;
import com.erpapi.gzerp.exceptions.InvalidProductException;
import com.erpapi.gzerp.models.MarketplaceListing;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.MarketplaceListingRepo;
import com.erpapi.gzerp.repositories.ProductsRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import com.erpapi.gzerp.security.TenantContext;
import jakarta.persistence.criteria.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.JpqlQueryBuilder;
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

    public ProductPageResponseDto getProductsPage(Pageable pageable, ProductFilterDto filterDto) {

        Long tenantId = TenantContext.required();

        Specification<Products> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            predicates.add(cb.isTrue(root.get("active")));


            if (filterDto.searchTerm() != null && !filterDto.searchTerm().isBlank()){
                String term = "%" + filterDto.searchTerm().toLowerCase() + "%";

                Predicate searchText = cb.or(
                        cb.like(cb.lower(root.get("name")),term),
                        cb.like(cb.lower(root.get("sku")), term)
                );


                List<Marketplace> enumsCompatible = Arrays.stream(Marketplace.values())
                        .filter(m -> m.name().toLowerCase().contains(filterDto.searchTerm().toLowerCase())).toList();


                if (!enumsCompatible.isEmpty()){
                    Predicate marketplaceMatch = existsListing(root, query, cb, enumsCompatible);
                    searchText = cb.or(searchText,marketplaceMatch);
                }
                predicates.add(searchText);
            }

            if (filterDto.hasListings() != null){
                Predicate exists = existsListing(root,query,cb,List.of());
                predicates.add(filterDto.hasListings() ? exists : cb.not(exists));
            }

            if (filterDto.marketplace() != null){
                predicates.add(existsListing(root,query,cb,List.of(filterDto.marketplace())));
            }

            if (filterDto.stock() != null){

                if (filterDto.stock() == StockFilter.LOW){
                    predicates.add(cb.lessThanOrEqualTo(
                            root.get("stock"),
                            root.get("minStock")
                    ));
                    predicates.add(cb.greaterThan(root.get("stock"), 0));
                }

                if (filterDto.stock() == StockFilter.OUT){
                    predicates.add(cb.lessThanOrEqualTo(root.get("stock"),0));
                }
                if (filterDto.stock() == StockFilter.NORMAL){
                    predicates.add(cb.greaterThan(root.get("stock"), root.get("minStock")));
                }
            }


            return cb.and(predicates.toArray(new Predicate[0]));

        };

        Page<Products> page = productsRepo.findAll(spec,pageable);

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
                    dto.setMinStock(product.getMinStock());

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




    private Predicate existsListing(Root<Products> root,
                                    CriteriaQuery<?> query,
                                    CriteriaBuilder cb,
                                    Collection<Marketplace> marketplaces){

        Subquery<Long> sub = query.subquery(Long.class);

        Root<MarketplaceListing> subRoot = sub.from(MarketplaceListing.class);

        sub.select(subRoot.get("id"));

        Predicate correlation = cb.equal(subRoot.get("product"),root);

        if (marketplaces != null && !marketplaces.isEmpty()){
            Predicate marketplaceFilter = subRoot.get("marketplace").in(marketplaces);
            sub.where(cb.and(correlation, marketplaceFilter));
        }else {
            sub.where(correlation);
        }

        return cb.exists(sub);
    }


}