package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantsRepo extends JpaRepository<Tenants, Long> {

    boolean existsByEmailAndId(String email, Long tenantId);

    boolean existsByEmail(String email);

    boolean existsByCnpj(String cnpj);

    boolean existsByRazaoSocial(String razaoSocial);

    Partners findByTenantId(Long tenantId);
    


}
