package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantsRepo extends JpaRepository<Tenants, Long> {

    boolean existsByEmailAndId(String email, Long id);

    boolean existsByEmail(String email);

    boolean existsByCnpj(String cnpj);

    boolean existsByRazaoSocial(String razaoSocial);

    Optional<Tenants> findById(long id);

    


}
