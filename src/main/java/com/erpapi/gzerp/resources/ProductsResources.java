package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.config.CustomUserDetails;
import com.erpapi.gzerp.dto.ProductRegisterDto;
import com.erpapi.gzerp.dto.ProductResponseDto;
import com.erpapi.gzerp.models.Products;
import com.erpapi.gzerp.repositories.ProductsRepo;
import com.erpapi.gzerp.services.ProductsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> RegisterProducts(@RequestBody @Valid ProductRegisterDto dto
                                                               , @AuthenticationPrincipal CustomUserDetails user){
        ProductResponseDto newProduct = service.registerProduct(dto, user.getUser());

        URI location = URI.create("/products/" + newProduct.getPublicId());
        return  ResponseEntity.created(location).body(newProduct);



    };


}
