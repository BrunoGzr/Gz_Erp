package com.erpapi.gzerp.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequestDto(
        @NotBlank @Size(max = 120) String name
        ) {
}
