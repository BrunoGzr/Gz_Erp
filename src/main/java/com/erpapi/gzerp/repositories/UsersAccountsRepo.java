package com.erpapi.gzerp.repositories;


import com.erpapi.gzerp.models.UsersAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UsersAccountsRepo extends JpaRepository<UsersAccounts, Long> {

    boolean existsByEmail(String email, Long tenantId);

    Optional<UsersAccounts> findByUsername(String username);
    Optional<UsersAccounts> findByUsernameAndPassword(String username, String password);
    Optional<UsersAccounts> findByIdAndTenantId (Long id, Long tenantId);
    Optional<UsersAccounts> findByEmailAndTenantId(String email, Long tenantId);

    Optional<UsersAccounts> findByUsernameOrEmail(String username, String email);
}
