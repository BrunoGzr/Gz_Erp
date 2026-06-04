package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepo extends JpaRepository<Users, Long> {

}
