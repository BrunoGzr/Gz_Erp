package com.erpapi.gzerp.repositories;


import com.erpapi.gzerp.models.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepo extends JpaRepository<Roles, Long> {

    Optional<Roles> findByIdAndIsSystemTrue(Long id);
    Optional<Roles> findByNameAndIsSystemTrue(String name);
}
