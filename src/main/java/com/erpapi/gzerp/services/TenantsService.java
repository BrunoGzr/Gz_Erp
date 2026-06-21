package com.erpapi.gzerp.services;

import com.erpapi.gzerp.repositories.TenantsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TenantsService {

    private final TenantsRepo tenantsRepo;

    public TenantsService(TenantsRepo tenantsRepo) {
        this.tenantsRepo = tenantsRepo;
    }




}
