package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.security.TenantContext;
import com.erpapi.gzerp.services.ShopeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/marketplaces")
public class MarketplacesResource {

    private final ShopeeService shopeeService;

    public MarketplacesResource(ShopeeService shopeeService) {
        this.shopeeService = shopeeService;
    }


    @GetMapping("/callback/shopee")
    public ResponseEntity<?> shopeeCallback(
            @RequestParam String code,
            @RequestParam(name = "shop_id") Long shopId,
            @RequestParam(required = false) String state){

       shopeeService.handleCallback(code, shopId, state);
       return ResponseEntity.ok("Autorização deu certo");
    }

    @PreAuthorize("hasAuthority('PERM_SHOPEE_OVERVIEW')")
    @PostMapping("/account/{publicId}/renew")
    public ResponseEntity<?> testeRenovaçaoToken(@PathVariable UUID publicId){
        shopeeService.handleRenewToken(publicId);

        return ResponseEntity.ok("Deu certo.");
    }

}
