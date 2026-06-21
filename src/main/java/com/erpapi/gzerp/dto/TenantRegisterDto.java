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

    @NotNull
    @Size(min = 14, max = 14)
    private String cnpj;

    @NotNull
    @Email
    @Size(min = 5, max = 254)
    private String email;

    @NotNull
    @Size(min = 6, max = 180)
    private String razaoSocial;

    @Nullable()
    private String nomeFantasia;

    @NotEmpty(message = "Please inform the company partners")
    @Valid
    private ArrayList<PartnersRegisterDto> partners;
}
