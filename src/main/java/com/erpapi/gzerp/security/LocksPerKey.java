package com.erpapi.gzerp.security;

import org.springframework.stereotype.Component;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LocksPerKey {

    private final ConcurrentHashMap<Long,  Lock> locks = new ConcurrentHashMap<>();

    public Lock getLock(Long key){
        return locks.computeIfAbsent(key,k -> new ReentrantLock());
    }

    public void freeLock(Long key){
        locks.remove(key);
    }

}
