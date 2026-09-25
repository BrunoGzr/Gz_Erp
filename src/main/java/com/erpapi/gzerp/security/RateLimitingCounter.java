package com.erpapi.gzerp.security;

public class RateLimitingCounter {

    private int total;
    private long expiresin;


    public RateLimitingCounter(int total, long expiresin) {
        this.total = total;
        this.expiresin = expiresin;
    }

    public int addToTotal() {
        return this.total++;
    }

    public int getTotal() {
        return total;
    }

    public long getExpiresin() {
        return expiresin;
    }

}
