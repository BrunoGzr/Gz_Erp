package com.erpapi.gzerp.dto.categories;

import com.erpapi.gzerp.enums.CategoryType;

import java.util.UUID;

public record CategoryResponseDto(
        String name,
        UUID publicId,
        CategoryType type
) {
}
