package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.config.CustomUserDetails;
import com.erpapi.gzerp.dto.PageResponseDto;
import com.erpapi.gzerp.dto.ProductRegisterDto;
import com.erpapi.gzerp.dto.ProductResponseDto;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.repositories.ProductsRepo;
import com.erpapi.gzerp.security.TenantContext;
import com.erpapi.gzerp.services.ProductsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/products")
public class ProductsResources {

    private final ProductsService service;
    private final ProductsRepo repo;

    public ProductsResources(ProductsService service, ProductsRepo repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('PERM_CREATE_PROD')")
    public ResponseEntity<?> registerProducts(@RequestBody @Valid ProductRegisterDto dto
                                                               , @AuthenticationPrincipal CustomUserDetails user){
        ProductResponseDto newProduct = service.registerProduct(dto, user.getUser());

        URI location = URI.create("/products/" + newProduct.getPublicId());
        return  ResponseEntity.created(location).body(newProduct);
    };

    @GetMapping("/products")
    @PreAuthorize("hasAuthority('PERM_VIEW_PROD')")
    public ResponseEntity<PageResponseDto> getProductsListing(@PageableDefault(
            size = 20
            ,sort = "name"
            ,direction = Sort.Direction.ASC)
                Pageable pageable){
        return ResponseEntity.ok().body(service.productPagesGet(pageable));
    }

}
