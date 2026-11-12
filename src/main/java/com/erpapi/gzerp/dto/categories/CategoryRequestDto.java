package com.erpapi.gzerp.dto.categories;

import com.erpapi.gzerp.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
        @NotBlank @Size(max = 100) String name,
        @NotNull CategoryType type
        ) {
}
