package com.erpapi.gzerp.services;

import com.erpapi.gzerp.exceptions.UserAlreadyExistException;
import com.erpapi.gzerp.dto.PartnersRegisterDto;
import com.erpapi.gzerp.dto.TenantRegisterDto;
import com.erpapi.gzerp.dto.TenantResponseDto;
import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.enums.Status;
import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TenantsService {


    private final TenantsRepo tenantsRepo;
    private final EmployeesRepo employeesRepo;

    public TenantsService(TenantsRepo tenantsRepo, EmployeesRepo employeesRepo) {
        this.tenantsRepo = tenantsRepo;
        this.employeesRepo = employeesRepo;
    }


    @Transactional
    public TenantResponseDto registerTenant(TenantRegisterDto dto){
        List<String> conflictedFields = new ArrayList<>();
        if (tenantsRepo.existsByEmail(dto.getEmail())){
            conflictedFields.add("The Email already exists, please make the login");}
        if (tenantsRepo.existsByCnpj(dto.getCnpj())){
            conflictedFields.add("The cnpj already exists, please make the login");}
        if (tenantsRepo.existsByRazaoSocial(dto.getRazaoSocial())){
            conflictedFields.add("The razao social already exists");}
        if (!conflictedFields.isEmpty()){
           throw new UserAlreadyExistException(conflictedFields);}
        Tenants newTenant = new Tenants();
        newTenant.setCnpj(dto.getCnpj());
        newTenant.setEmail(dto.getEmail());
        newTenant.setRazaoSocial(dto.getRazaoSocial());
        newTenant.setDemo(false);
        newTenant.setPlan(Plans.FREE);
        newTenant.setStatus(Status.ACTIVE);
        newTenant.setisAdmin(false);
        newTenant.setPhone(dto.getPhone());

        if (dto.getNomeFantasia() != null) {
            newTenant.setNomeFantasia(dto.getNomeFantasia());}

        for (PartnersRegisterDto partnerDto : dto.getPartners()) {
            Partners newPartner = new Partners();
            newPartner.setEmail(partnerDto.getEmail());
            newPartner.setCpf(partnerDto.getCpf());
            newPartner.setFullName(partnerDto.getName());
            newPartner.setPhone(partnerDto.getPhone());
            newPartner.setSalary(partnerDto.getSalary());
            if (partnerDto.getOwnership() != null){
                newPartner.setOwnership(partnerDto.getOwnership());
            };
            newTenant.addPartner(newPartner);
        };
        Tenants savedTenant = tenantsRepo.save(newTenant);
        return new TenantResponseDto (savedTenant);
    }




}
