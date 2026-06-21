package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.TenantRegisterDto;
import com.erpapi.gzerp.repositories.TenantsRepo;
import com.erpapi.gzerp.services.TenantsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegisterResource {

    private final TenantsRepo tenantsRepo;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private final TenantsService tenantsService;


    public RegisterResource(TenantsService tenantsService, TenantsRepo tenantsRepo) {
        this.tenantsRepo = tenantsRepo;
        this.tenantsService = tenantsService;
    }


    @PostMapping
    public ResponseEntity<Object> RegisterTenant(@Valid @RequestBody TenantRegisterDto dto)  {



    }


}
