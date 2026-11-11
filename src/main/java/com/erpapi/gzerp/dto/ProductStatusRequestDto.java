package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductStatusRequestDto(
       @NotNull UUID publicId,
       @NotNull ProductStatus status
) {
}
