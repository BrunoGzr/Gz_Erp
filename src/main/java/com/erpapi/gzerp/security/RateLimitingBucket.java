package com.erpapi.gzerp.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingBucket {

    private final Map<String, RateLimitingCounter> map = new ConcurrentHashMap<>();

    public boolean allow(String key, int limit,long windowsMs){
        long now = System.currentTimeMillis();

        RateLimitingCounter counter = map.compute(key,(k, existence) ->{
            if( existence == null || existence.getExpiresin() < now ){
                return new RateLimitingCounter(1,now + windowsMs);
            }
            existence.addToTotal();
            return existence;
        } );

        return counter.getTotal() <= limit;



    }

    @Scheduled(fixedRate = 60_000)
    public void limpar() {
        long agora = System.currentTimeMillis();
        map.entrySet().removeIf(e -> e.getValue().getExpiresin() < agora);
    }



}
