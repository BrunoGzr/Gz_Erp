package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.services.ShopeeService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marketplaces")
public class MaketplacesResource {

    private final ShopeeService shopeeService;

    public MaketplacesResource(ShopeeService shopeeService) {
        this.shopeeService = shopeeService;
    }


    @GetMapping("/callback/shopee")
    public ResponseEntity<Void> shopeeCallback(
            @RequestParam String code,
            @RequestParam(name = "shop_id") Long shopId,
            @RequestParam String state){

       shopeeService.handleCallback(code, shopId, state);
       return ResponseEntity.status(HttpStatus.OK).build();
    }

}
