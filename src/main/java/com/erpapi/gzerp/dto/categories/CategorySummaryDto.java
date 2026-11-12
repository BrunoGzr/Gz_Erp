package com.erpapi.gzerp.dto.categories;

import com.erpapi.gzerp.enums.CategoryType;

import java.util.UUID;

public record CategorySummaryDto(
        UUID publicId,
        String name,
        CategoryType type)
{}