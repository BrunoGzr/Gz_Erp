package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;

import java.util.List;

public class TenantResponseDto {

    private String cnpj;
    private List<Partners> partners;
    private String email;
    private String razaoSocial;
    private String nomeFantasia;

    public TenantResponseDto() {}

    public TenantResponseDto(Tenants tenant) {
        this.cnpj = tenant.getCnpj();
        this.email = tenant.getEmail();
        this.razaoSocial = tenant.getRazaoSocial();
        this.nomeFantasia = tenant.getNomeFantasia();
    }

    public String getCnpj() {
        return cnpj;
    }
    public List<Partners> getPartners() {
        return partners;
    }
    public String getEmail() {
        return email;
    }
    public String getRazaoSocial() {
        return razaoSocial;
    }
    public String getNomeFantasia() {
        return nomeFantasia;
    }
}
