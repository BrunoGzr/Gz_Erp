package com.erpapi.gzerp.services;

import com.erpapi.gzerp.exceptions.EmployeeAlreadyExistException;
import com.erpapi.gzerp.dto.PartnersRegisterDto;
import com.erpapi.gzerp.dto.TenantRegisterDto;
import com.erpapi.gzerp.dto.TenantResponseDto;
import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.enums.Status;
import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;
import com.erpapi.gzerp.models.UsersAccounts;
import com.erpapi.gzerp.repositories.EmployeesRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TenantsService {


    private final TenantsRepo tenantsRepo;
    private final PartnersService partnersService;
    private final UsersAccountsService usersAccountsService;

    public TenantsService(TenantsRepo tenantsRepo, PartnersService partnersService, UsersAccountsService usersAccountsService) {
        this.tenantsRepo = tenantsRepo;
        this.partnersService = partnersService;
        this.usersAccountsService = usersAccountsService;
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
           throw new EmployeeAlreadyExistException(conflictedFields);}
        Tenants newTenant = new Tenants();
        newTenant.setCnpj(dto.getCnpj());
        newTenant.setEmail(dto.getEmail());
        newTenant.setRazaoSocial(dto.getRazaoSocial());
        newTenant.setDemo(false);
        newTenant.setPlan(Plans.FREE);
        newTenant.setStatus(Status.ACTIVE);
        newTenant.setIsAdmin(false);
        newTenant.setPhone(dto.getPhone());
        if (dto.getNomeFantasia() != null) {
            newTenant.setNomeFantasia(dto.getNomeFantasia());}
        newTenant = tenantsRepo.save(newTenant);
        for (PartnersRegisterDto partnerDto : dto.getPartners()) {
            String dtoUsername = partnerDto.getUsername();
            UsersAccounts newUser = usersAccountsService.userRegisterPartner(partnerDto, newTenant);
            Partners newPartner = partnersService.registerPartners(partnerDto,newUser, newTenant);
            newTenant.addPartner(newPartner);
        };

        return new TenantResponseDto (newTenant);
    }




}
