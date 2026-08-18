package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnersRepo extends JpaRepository<Partners, Long> {

    List<Partners> findByTenant_Id(Long tenantId);
    


}
