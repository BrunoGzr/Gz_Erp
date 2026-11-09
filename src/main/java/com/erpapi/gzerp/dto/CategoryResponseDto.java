package com.erpapi.gzerp.dto;

import java.util.List;
import java.util.UUID;

public record CategoryResponseDto(
        String name,
        UUID publicId
) {
}
