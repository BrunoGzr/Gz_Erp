package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.enums.Plans;
import com.erpapi.gzerp.models.Partners;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;
import java.util.ArrayList;

public class TenantRegisterDto {

    @NotEmpty(message = "Please inform the Tenant: CNPJ")
    @Size(min = 1, max = 100)
    private String cnpj;

    @NotEmpty(message = "Please inform the Tenant: Email")
    @Email
    @Size(min = 5, max = 254)
    private String email;

    @NotEmpty(message = "Please inform the Tenant: Razao Social")
    @Size(min = 6, max = 180)
    private String razaoSocial;

    @Nullable()
    private String nomeFantasia;

    @NotEmpty(message = "Please inform at least one company partner")
    @Valid
    private ArrayList<PartnersRegisterDto> partners;

    @NotEmpty(message = "Please inform the Tenant registered Phone")
    @Size(min = 11, max = 15)
    private String phone;

    public TenantRegisterDto() {}

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    @Nullable
    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(@Nullable String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public ArrayList<PartnersRegisterDto> getPartners() {
        return partners;
    }

    public void setPartners(ArrayList<PartnersRegisterDto> partners) {
        this.partners = partners;
    }
}
