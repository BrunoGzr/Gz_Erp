package com.erpapi.gzerp.dto.Users.Tenants;

import com.erpapi.gzerp.dto.Users.Partners.PartnersResponseDto;
import com.erpapi.gzerp.models.Partners;
import com.erpapi.gzerp.models.Tenants;

import java.util.ArrayList;

public class TenantResponseDto {

    private String cnpj;
    private ArrayList<PartnersResponseDto> partners;
    private String email;
    private String razaoSocial;
    private String nomeFantasia;

    public TenantResponseDto() {}

    public TenantResponseDto(Tenants tenant) {
        this.cnpj = tenant.getCnpj();
        this.email = tenant.getEmail();
        this.razaoSocial = tenant.getRazaoSocial();
        this.nomeFantasia = tenant.getNomeFantasia();
        this.partners = new ArrayList<>();
        for (Partners partner : tenant.getPartners()) {
            PartnersResponseDto newPartner = new PartnersResponseDto(partner);
            this.partners.add(newPartner);
        }
    }

    public String getCnpj() {
        return cnpj;
    }
    public ArrayList<PartnersResponseDto> getPartners() {
        return partners;
    }

    public void setPartners(ArrayList<PartnersResponseDto> partners) {
        this.partners = new ArrayList<>();
        for (PartnersResponseDto partner : partners) {
            this.partners.add(partner);
        }
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
