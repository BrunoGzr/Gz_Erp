package com.erpapi.gzerp.jobs;

import com.erpapi.gzerp.repositories.RefreshTokenRepo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);

    private static final int RETENTION_DAYS = 30;

    private final RefreshTokenRepo refreshTokenRepo;

    public RefreshTokenCleanupJob(RefreshTokenRepo refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens(){
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);

        int deleted = refreshTokenRepo.deleteExpiredBefore(cutoff);

        if (deleted > 0 ){
            log.info("Refresh token cleanp: {} expired tokens removed. (cutoff: {})", deleted,cutoff);
        }else {
            log.debug("Refresh token cleanup: no tokens to cleanup.");
        }

    }
}
