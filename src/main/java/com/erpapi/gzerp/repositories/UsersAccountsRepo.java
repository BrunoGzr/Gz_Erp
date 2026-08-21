package com.erpapi.gzerp.repositories;


import com.erpapi.gzerp.models.UsersAccounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersAccountsRepo extends JpaRepository<UsersAccounts, Long> {

    boolean existsByEmail(String email, Long tenantId);
    UsersAccounts findByIdAndTenantId (Long id, Long tenantId);
    UsersAccounts findByEmailAndTenantId(String email, Long tenantId);
}
