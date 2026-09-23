package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.RefreshToken;
import com.erpapi.gzerp.models.UsersAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userAccount = :user AND rt.revoked = false")
    void revokeAllByUser(@Param("user") UsersAccounts user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);

}
