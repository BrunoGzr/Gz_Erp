package com.erpapi.gzerp.dto;

import java.util.UUID;

public record CategorySummaryDto(
        UUID publicId,
        String name)
{}