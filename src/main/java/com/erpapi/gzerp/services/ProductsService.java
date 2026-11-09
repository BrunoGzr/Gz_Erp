package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.*;
import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.enums.StockFilter;
import com.erpapi.gzerp.exceptions.*;
import com.erpapi.gzerp.models.Categories;
import com.erpapi.gzerp.models.MarketplaceListing;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.repositories.*;
import com.erpapi.gzerp.security.TenantContext;
import jakarta.persistence.criteria.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductsService {

    private final ProductsRepo productsRepo;
    private final TenantsRepo tenantsRepo;
    private final MarketplaceListingRepo listingRepo;
    private final CategoryRepo categoryRepo;
    private final DefaultCategoryRepo defaultCategoryRepo;


    public ProductsService(ProductsRepo productsRepo, TenantsRepo tenantsRepo, MarketplaceListingRepo listingRepo, CategoryRepo categoryRepo, DefaultCategoryRepo defaultCategoryRepo) {
        this.productsRepo = productsRepo;
        this.tenantsRepo = tenantsRepo;
        this.listingRepo = listingRepo;
        this.categoryRepo = categoryRepo;
        this.defaultCategoryRepo = defaultCategoryRepo;
    }

    public ProductResponseDto registerProduct(@Valid ProductRegisterDto dto) {
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

    public void verifyUseUpdate(ProductUpdateDto dto, Long tenantId, Products product){

        if (productsRepo.existsBySkuAndTenant_IdAndIdNot(dto.getSku(), tenantId, product.getId())){
            throw new InvalidProductException("Product Sku already in use, please use another one.");
        }
        if (productsRepo.existsByNameAndTenantIdAndIdNot(dto.getName(),tenantId, product.getId())){
            throw  new InvalidProductException("Product Name already in use, please use another one.");
        }
    }

    public ProductPageResponseDto getProductsPage(Pageable pageable, ProductFilterDto filterDto) {

        Long tenantId = TenantContext.required();

        List<Long> categoryIdsInternal =
                (filterDto.categoryIds() == null || filterDto.categoryIds().isEmpty())
                        ? List.of()
                        : categoryRepo.findIdsByPublicIdsAndTenantId(filterDto.categoryIds(), tenantId);

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

            if (!categoryIdsInternal.isEmpty()) {
                predicates.add(existsCategory(root, query, cb, categoryIdsInternal));
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

        List<CategoryByProductProjection> rawCategories = categoryRepo.findCategoriesByProductIds(productsIds);

        Map<Long, List<CategorySummaryDto>> categoriesByProductId = new HashMap<>();

        for (CategoryByProductProjection row : rawCategories) {
            categoriesByProductId
                    .computeIfAbsent(row.getProductId(), k -> new ArrayList<>())
                    .add(new CategorySummaryDto(
                            UUID.fromString(row.getPublicId()),
                            row.getName()
                    ));
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

                    dto.setCategories(categoriesByProductId.getOrDefault(product.getId(), List.of()));


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



    private Predicate existsCategory(Root<Products> root,
                                     CriteriaQuery<?> query,
                                     CriteriaBuilder cb,
                                     Collection<Long> categoryIds) {

        Subquery<Long> sub = query.subquery(Long.class);
        Root<Products> subRoot = sub.from(Products.class);
        Join<Products, Categories> catJoin = subRoot.join("categories", JoinType.INNER);

        sub.select(subRoot.get("id"))
                .where(
                        cb.equal(subRoot, root),
                        catJoin.get("id").in(categoryIds)
                );

        return cb.exists(sub);
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

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto){

        Long tenantId = TenantContext.required();
        if (dto.name().isBlank()) {
            throw new InvalidCategoryException("Nome não pode ser vazio.");
        }
        if (categoryRepo.existsByNameAndTenant_Id(dto.name(), tenantId)) {
            throw new InvalidCategoryException("Category already exists.");
        }

        Categories newCategory = new Categories();

        newCategory.setActive(true);
        newCategory.setName(dto.name());
        newCategory.setPublicId(UUID.randomUUID());
        newCategory.setTenant(tenantsRepo.getReferenceById(tenantId));
        Categories savedCategory = categoryRepo.save(newCategory);

        return new CategoryResponseDto(
                savedCategory.getName(),
                savedCategory.getPublicId()
        );
    }

    public List<CategoryResponseDto> getCategories(){
        Long tenantId = TenantContext.required();


        return categoryRepo.findAllByActiveTrueAndTenant_IdOrderByNameAsc(tenantId).stream().map(
                cat -> new CategoryResponseDto(
                        cat.getName(),
                        cat.getPublicId()
                )).toList();

    }

    @Transactional
    public ProductResponseDto updateProduct(ProductUpdateDto dto, UUID publicId){
        Long tenantId = TenantContext.required();
        Products actualProduct = productsRepo.findByPublicIdAndTenant_IdAndActiveTrue(publicId,tenantId).orElseThrow(
                () -> new UnknownProductException("Product not found for Update, please enter a valid one.")
        );

        verifyUseUpdate(dto,tenantId,actualProduct);

        if (dto.getName() != null && !dto.getName().isBlank()){
            String name = dto.getName().replace("(?U)\\s", "").trim();
            actualProduct.setName(name);
        }

        if (dto.getSku() == null || dto.getSku().isBlank()){
            actualProduct.setSku(generateRandomSku(actualProduct));
        }
        else {
            String sku = dto.getSku().replace("(?U)\\s", "").trim();
            actualProduct.setSku(sku);
        }

        if (dto.getBaseSellPrice() != null){
            actualProduct.setBaseSellPrice(dto.getBaseSellPrice());
        }

        if (dto.getCategoryIds() != null){
            if (dto.getCategoryIds().isEmpty()){
                actualProduct.getCategories().clear();
            }else {
                List<UUID> requested = dto.getCategoryIds().stream().distinct().toList();

                List<Categories> resolvedCategories = categoryRepo.findAllByActiveTrueAndTenant_IdAndPublicIdIn(tenantId,dto.getCategoryIds());

                if (resolvedCategories.size() != requested.size()){
                    Set<UUID> found = resolvedCategories.stream().map(Categories::getPublicId).collect(Collectors.toSet());

                    List<UUID> missing = dto.getCategoryIds().stream()
                            .filter(id -> !found.contains(id))
                            .toList();

                    throw new InvalidCategoryException("Categories invalid or inactive: " + missing);
                }

                actualProduct.getCategories().clear();
                actualProduct.getCategories().addAll(resolvedCategories);
            }
        }

        if (dto.getImageUrl() != null){
            String j = dto.getImageUrl();
            actualProduct.setImageUrl( j.isBlank()? null : j );
        }


        if (dto.getCostPrice() != null){
            actualProduct.setCostPrice(dto.getCostPrice());
        }

        if (dto.getMinStock() != null){
            actualProduct.setMinStock(dto.getMinStock());
        }

        if (dto.getStock() != null){
            actualProduct.setStock(dto.getStock());
        }

        actualProduct = productsRepo.save(actualProduct);

        List<ListingCountProjectionDto> rawCounts = listingRepo.countListingsByProductIds(List.of(actualProduct.getId()));

        Map<String, Long> listingCount = new HashMap<>();

        for (ListingCountProjectionDto row : rawCounts){
            listingCount.put(row.getMarketplace(),row.getTotal());
        }

        ProductResponseDto response = new ProductResponseDto();

        response.setPublicId(actualProduct.getPublicId());
        response.setSku(actualProduct.getSku());
        response.setName(actualProduct.getName());
        response.setStock(actualProduct.getStock());
        response.setBaseSellPrice(actualProduct.getBaseSellPrice());
        response.setCostPrice(actualProduct.getCostPrice());
        response.setImageUrl(actualProduct.getImageUrl());
        response.setSales(actualProduct.getSales());
        response.setCreatedAt(actualProduct.getCreatedAt());
        response.setUpdatedAt(actualProduct.getUpdatedAt());
        response.setListingCount(listingCount);
        response.setMinStock(actualProduct.getMinStock());
        response.setCategories(actualProduct.getCategories().stream().map(category -> {
            return new CategorySummaryDto(category.getPublicId(),category.getName());
        }).toList());

        return response;














    }

    private String generateRandomSku(Products product){

        String name = product.getName().trim().split("\\s+")[0];
        if (product.getName().length() > 43){
            name = product.getName().substring(0,43);
        }

        String firstName = name.split(" ")[0];
        String randomChars = UUID.randomUUID().toString().substring(0,6);

        return firstName + "-" + randomChars;

    }

    @Transactional
    public void softDeleteProduct(UUID publicId){

        Long tenantId = TenantContext.required();

        Products product = productsRepo.findByPublicIdAndTenant_IdAndActiveTrue(publicId,tenantId).orElseThrow(
                () -> new EntityNotFoundException("Product")
        );

        product.setActive(false);

        listingRepo.deactivateByProductPublicid(product.getPublicId());
        productsRepo.save(product);



    }


}
