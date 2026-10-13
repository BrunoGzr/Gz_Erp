package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.services.ShopeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
