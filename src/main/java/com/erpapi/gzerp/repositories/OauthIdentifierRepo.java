package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.OauthIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OauthIdentifierRepo extends JpaRepository<OauthIdentifier, Long> {


    Optional<OauthIdentifier> findByState(UUID state);

}
