package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.*;
import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.enums.ProductStatus;
import com.erpapi.gzerp.enums.StockFilter;
import com.erpapi.gzerp.exceptions.*;
import com.erpapi.gzerp.models.Categories;
import com.erpapi.gzerp.models.MarketplaceListing;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.repositories.*;
import com.erpapi.gzerp.security.TenantContext;
import jakarta.persistence.criteria.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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

    public void verifyUse(ProductRegisterDto dto, Long tenantId){

        String sku = dto.getSku() == null ? null : dto.getSku().trim();
        String name = dto.getName() == null ? null : dto.getName().trim();

        if (sku != null && productsRepo.existsBySkuAndTenantId(sku, tenantId)){
            throw new InvalidProductException("Product Sku already in use, please use another one.");
        }
        if (name != null && productsRepo.existsByNameAndTenantId(name,tenantId)){
            throw  new InvalidProductException("Product Name already in use, please use another one.");
        }
    }

    public void verifyUseUpdate(ProductUpdateDto dto, Long tenantId, Products product){

        String sku = dto.getSku() == null ? null : dto.getSku().trim();
        String name = dto.getName() == null ? null : dto.getName().trim();

        if (sku != null && productsRepo.existsBySkuAndTenant_IdAndIdNot(sku, tenantId, product.getId())){
            throw new InvalidProductException("Product Sku already in use, please use another one.");
        }
        if (name != null && productsRepo.existsByNameAndTenantIdAndIdNot(name,tenantId, product.getId())){
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
                    dto.setStatus(product.getStatus());
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
        Predicate isActive =cb.isTrue(subRoot.get("active"));

        Predicate predicate = cb.and(correlation,isActive);

        if (marketplaces != null && !marketplaces.isEmpty()) {
            Predicate marketplaceFilter = subRoot.get("marketplace").in(marketplaces);
            predicate = cb.and(predicate, marketplaceFilter);
        }

        sub.where(predicate);

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

        if (dto.getStatus() != null && !dto.getStatus().name().isBlank()){
            actualProduct.setStatus(dto.getStatus());
        }

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

        try {
            actualProduct = productsRepo.saveAndFlush(actualProduct);
        } catch (DataIntegrityViolationException exception){
            throw new InvalidProductException("Product Name or Sku already in use, please use another one");

        }


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
        response.setStatus(actualProduct.getStatus());


        return response;
    }

    private String generateRandomSku(Products product){

        String firstName = product.getName().trim().split("\\s+")[0];
        if (firstName.length() > 43){
            firstName = firstName.substring(0,43);
        }

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

    @Transactional
    public ProductResponseDto createProduct(ProductRegisterDto dto){

        Long tenantId = TenantContext.required();

        Tenants tenant = tenantsRepo.findById(tenantId).orElseThrow(() -> new TenantNotFoundException(tenantId));

        verifyUse(dto,tenant.getId());

        Products newProduct = new Products();

        newProduct.setTenant(tenant);
        newProduct.setPublicId(UUID.randomUUID());
        newProduct.setStatus(ProductStatus.ACTIVE);
        newProduct.setActive(true);
        newProduct.setImageUrl(dto.getImageUrl() == null || dto.getImageUrl().isBlank() ? null : dto.getImageUrl());
        newProduct.setName(dto.getName().trim());
        newProduct.setSku(dto.getSku() == null || dto.getSku().isBlank()? generateRandomSku(newProduct) : dto.getSku().trim());
        newProduct.setStock(dto.getStock());
        LocalDateTime now = LocalDateTime.now();
        newProduct.setCreatedAt(now);
        newProduct.setUpdatedAt(now);
        newProduct.setMinStock(dto.getMinStock() == null ? 20 :dto.getMinStock());
        newProduct.setCostPrice(dto.getCostPrice());

        List<UUID> requested = dto.getCategoryIds().stream().distinct().toList();

        List<Categories> resolvedCategories = categoryRepo.findAllByActiveTrueAndTenant_IdAndPublicIdIn(tenantId,dto.getCategoryIds());

        if (resolvedCategories.size() != requested.size()) {
            Set<UUID> found = resolvedCategories.stream().map(Categories::getPublicId).collect(Collectors.toSet());

            List<UUID> missing = dto.getCategoryIds().stream()
                    .filter(id -> !found.contains(id))
                    .toList();

            throw new InvalidCategoryException("Categories invalid or inactive: " + missing);
        }

        newProduct.setCategories(new HashSet<>(resolvedCategories));
        newProduct.setBaseSellPrice(dto.getBaseSellPrice());
        newProduct.setSales(0);

        try {
            newProduct = productsRepo.saveAndFlush(newProduct);
        } catch (DataIntegrityViolationException exception){
            throw new InvalidProductException("Product Name or Sku already in use, please use another one");
        }

        ProductResponseDto response = new ProductResponseDto();

        response.setPublicId(newProduct.getPublicId());
        response.setSku(newProduct.getSku());
        response.setName(newProduct.getName());
        response.setStock(newProduct.getStock());
        response.setBaseSellPrice(newProduct.getBaseSellPrice());
        response.setCostPrice(newProduct.getCostPrice());
        response.setImageUrl(newProduct.getImageUrl());
        response.setSales(newProduct.getSales());
        response.setCreatedAt(newProduct.getCreatedAt());
        response.setUpdatedAt(newProduct.getUpdatedAt());
        response.setListingCount(new HashMap<>());
        response.setMinStock(newProduct.getMinStock());
        response.setCategories(newProduct.getCategories().stream().map(
                category -> new CategorySummaryDto(category.getPublicId(), category.getName())
        ).toList());
        response.setStatus(newProduct.getStatus());

        return response;
    }

    @Transactional
    public void updateStatus(List<ProductStatusRequestDto> list){
        Long tenantId = TenantContext.required();

        boolean haveNull = list.stream().anyMatch(Objects::isNull);

        if (haveNull){
            throw new IllegalArgumentException();
        }

        if (list.isEmpty()){
            throw new EntityNotFoundException("Products in list");
        }

        Map<UUID,ProductStatusRequestDto> dtoByPublicID = list.stream()
                .collect(Collectors.toMap(
                        ProductStatusRequestDto::publicId,
                        Function.identity(),
                        (a,b)-> b));

        List<UUID> publicIds = new ArrayList<>(dtoByPublicID.keySet());

        List<Products> products = productsRepo.findAllByTenant_IdAndActiveTrueAndPublicIdIn(tenantId,publicIds);

        if (products.size() != publicIds.size()) {
            Set<UUID> found = products.stream()
                    .map(product -> {
                        return product.getPublicId();
                    }).collect(Collectors.toSet());

            List<UUID> notFound = publicIds.stream().filter(id -> !found.contains(id)).toList();

            throw new EntityNotFoundException("Products not found: " + notFound);
        }

        for(Products prod : products){
            ProductStatusRequestDto dto = dtoByPublicID.get(prod.getPublicId());
            prod.setStatus(dto.status());
        }

        productsRepo.saveAll(products);
    }

    public ProductResponseDto getProductInfo(UUID publicId){
        Long tenantid = TenantContext.required();

        Products product = productsRepo.findByPublicIdAndTenant_IdAndActiveTrue(publicId,tenantid).orElseThrow(
                () -> new EntityNotFoundException("Product"));

        List<ListingCountProjectionDto> rawCounts = listingRepo.countListingsByProductIds(List.of(product.getId()));

        Map<String, Long> listingCount = new HashMap<>();

        for (ListingCountProjectionDto row : rawCounts){
            listingCount.put(row.getMarketplace(),row.getTotal());
        }

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
        dto.setListingCount(listingCount);
        dto.setMinStock(product.getMinStock());
        dto.setCategories(product.getCategories().stream().map(
                category -> {
                    return new CategorySummaryDto(category.getPublicId(),category.getName());
                }
        ).toList());
        dto.setStatus(product.getStatus());

        return dto;
    }



}
