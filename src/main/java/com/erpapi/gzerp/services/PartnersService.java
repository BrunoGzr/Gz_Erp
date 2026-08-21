package com.erpapi.gzerp.services;

import com.erpapi.gzerp.dto.PartnersRegisterDto;
import com.erpapi.gzerp.dto.PartnersResponseDto;
import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.PartnersRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class PartnersService {

    private final PartnersRepo partnersRepo;

    public PartnersService(PasswordEncoder passwordEncoder, PartnersRepo partnersRepo) {
        this.partnersRepo = partnersRepo;
    }

    public Partners registerPartners(PartnersRegisterDto dto, UsersAccounts user, Tenants tenant){
        Partners newPartner = new Partners();
        newPartner.setCpf(dto.getCpf());
        newPartner.setFullName(dto.getFullName());
        newPartner.setEmail(dto.getEmail());
        newPartner.setPhone(dto.getPhone());
        if (dto.getOwnership() != null){
            newPartner.setOwnership(dto.getOwnership());
        }
        if (dto.getSalary() != null){
            newPartner.setSalary(dto.getSalary());
        }

        newPartner.setUsersAccounts(user);
        newPartner.setTenant(tenant);
        newPartner = partnersRepo.save(newPartner);
        return newPartner;

    }

}
