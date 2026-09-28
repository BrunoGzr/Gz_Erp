package com.erpapi.gzerp.security;

public final class TenantContext {

    public static final ScopedValue<Long> TENANT_ID = ScopedValue.newInstance();

    private TenantContext() {
    }

    public static Long required(){
        if (!TENANT_ID.isBound()){
            throw new IllegalStateException("No tenant_Id in the scope.");
        }
        return TENANT_ID.get();
    }

    public static Long getOrNull(){
        return TENANT_ID.isBound() ? TENANT_ID.get() : null;
    }

}
