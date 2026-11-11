package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.*;


import com.erpapi.gzerp.exceptions.InvalidPageSizeException;

import com.erpapi.gzerp.services.ProductsService;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/products")
public class ProductsResources {

    private final ProductsService service;


    public ProductsResources(ProductsService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('PERM_CREATE_PROD')")
    public ResponseEntity<?> registerProducts(@RequestBody @Valid ProductRegisterDto dto){
        ProductResponseDto newProduct = service.createProduct(dto);

        URI location = URI.create("/api/products/" + newProduct.getPublicId());
        return  ResponseEntity.created(location).body(newProduct);
    }

    @GetMapping("/overall")
    public ResponseEntity<ProductPageResponseDto> getProductPage(
            @PageableDefault(
            size = 20
            ,sort = "name"
            ,direction = Sort.Direction.ASC)
            Pageable pageable,
            ProductFilterDto filter
    ){
            List<Integer> pageSizes = List.of(20,50,100,200);

            if (!pageSizes.contains(pageable.getPageSize())){
                throw new InvalidPageSizeException("" + pageable.getPageSize());
            }

            ProductPageResponseDto dto = service.getProductsPage(pageable, filter);

            return ResponseEntity.ok().body(dto);
    }


    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDto>> getCategories(){
        return ResponseEntity.ok().body(service.getCategories());
    }

    @PatchMapping("/{publicId}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @RequestBody @Valid ProductUpdateDto product,
            @PathVariable @Valid UUID publicId
            ){
        return ResponseEntity.ok().body(service.updateProductRequest(product,publicId));
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<Void> softDeleteProduct(
            @PathVariable UUID publicId
    ){
        service.softDeleteProduct(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/status")
    public ResponseEntity<?> updateStatus(
            @RequestBody @Valid List<@NotNull @Valid ProductStatusRequestDto> list
    ){

        service.updateStatus(list);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ProductResponseDto> getProductInfo(
            @Valid @PathVariable UUID publicId
    ){
        ProductResponseDto dto = service.getProductInfo(publicId);

        return ResponseEntity.ok().body(dto);
    }

    @PatchMapping("/bulk")
    public ResponseEntity<List<ProductResponseDto>> updateProductsBulk(
            @RequestBody @Valid List<@NotNull @Valid ProductUpdateDto> list
    ){
        return ResponseEntity.ok().body(service.updateProductsBulk(list));
    }










}
