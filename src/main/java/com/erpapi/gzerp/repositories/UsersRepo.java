package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepo extends JpaRepository<Users, Long> {

    boolean existsByEmail(String email, Long tenantId);
    Users findByEmail(String email, Long tenantId);

    boolean existsByUserName(String userName, Long tenantId);
    Users findByUserName(String userName, Long tenantId);
}
