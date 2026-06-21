package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.models.Tenants;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PartnersRegisterDto {

    @NotNull
    private Long id;

    @NotNull
    @Size(min = 11, max = 11)
    private String cpf;

    @NotEmpty
    private String fullName;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String phone;

    @Nullable
    private BigDecimal ownership;

    @Nullable
    private Float salary;


}
