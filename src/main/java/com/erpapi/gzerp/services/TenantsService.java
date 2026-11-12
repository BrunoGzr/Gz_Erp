package com.erpapi.gzerp.services;

import com.erpapi.gzerp.exceptions.EmployeeAlreadyExistException;
import com.erpapi.gzerp.dto.Users.Partners.PartnersRegisterDto;
import com.erpapi.gzerp.dto.Users.Tenants.TenantRegisterDto;
import com.erpapi.gzerp.dto.Users.Tenants.TenantResponseDto;
import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.enums.Status;
import com.erpapi.gzerp.models.*;
import com.erpapi.gzerp.repositories.CategoryRepo;
import com.erpapi.gzerp.repositories.DefaultCategoryRepo;
import com.erpapi.gzerp.repositories.TenantsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TenantsService {


    private final TenantsRepo tenantsRepo;
    private final PartnersService partnersService;
    private final UsersAccountsService usersAccountsService;
    private final AuthService authService;
    private final DefaultCategoryRepo defaultCategoryRepo;
    private final CategoryRepo categoryRepo;

    public TenantsService(TenantsRepo tenantsRepo, PartnersService partnersService, UsersAccountsService usersAccountsService, AuthService authService, DefaultCategoryRepo defaultCategoryRepo, CategoryRepo categoryRepo) {
        this.tenantsRepo = tenantsRepo;
        this.partnersService = partnersService;
        this.usersAccountsService = usersAccountsService;
        this.authService = authService;
        this.defaultCategoryRepo = defaultCategoryRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional
    public TenantResponseDto registerTenant(TenantRegisterDto dto){
        List<String> conflictedFields = new ArrayList<>();
        if (tenantsRepo.existsByEmail(dto.getEmail())){
            conflictedFields.add("The Email already exists, please make the login");}
        if (tenantsRepo.existsByCnpj(authService.cnpjFormater(dto.getCnpj()))){
            conflictedFields.add("The cnpj already exists, please make the login");}
        if (tenantsRepo.existsByRazaoSocial(dto.getRazaoSocial())){
            conflictedFields.add("The razao social already exists");}
        if (!conflictedFields.isEmpty()){
           throw new EmployeeAlreadyExistException(conflictedFields);}
        Tenants newTenant = new Tenants();
        newTenant.setCnpj(authService.cnpjFormater(dto.getCnpj()));
        newTenant.setEmail(dto.getEmail());
        newTenant.setRazaoSocial(dto.getRazaoSocial());
        newTenant.setDemo(false);
        newTenant.setPlan(Plans.FREE);
        newTenant.setStatus(Status.ACTIVE);
        newTenant.setIsAdmin(false);
        newTenant.setPhone(dto.getPhone());
        newTenant.setPublic_id(UUID.randomUUID());
        if (dto.getNomeFantasia() != null) {
            newTenant.setNomeFantasia(dto.getNomeFantasia());}
        newTenant = tenantsRepo.save(newTenant);
        for (PartnersRegisterDto partnerDto : dto.getPartners()) {
            UsersAccounts newUser = usersAccountsService.userRegisterPartner(partnerDto, newTenant);
            Partners newPartner = partnersService.registerPartners(partnerDto,newUser, newTenant);
            newTenant.addPartner(newPartner);
        };

        final Tenants savedTenant = tenantsRepo.save(newTenant);

        List<DefaultCategory> templates = defaultCategoryRepo.findAllByActiveTrue();

        List<Categories> newCategories = templates.stream().map(
                def -> {
                    Categories cat = new Categories();
                    cat.setPublicId(UUID.randomUUID());
                    cat.setName(def.getName());
                    cat.setTenant(savedTenant);
                    cat.setActive(true);
                    cat.setCategoryType(def.getCategoryType());
                    return cat;
                }).toList();

        categoryRepo.saveAll(newCategories);

        return new TenantResponseDto (newTenant);
    }
}
