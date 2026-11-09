package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.enums.StockFilter;

import java.util.List;
import java.util.UUID;

public record ProductFilterDto(
        String category,
        Marketplace marketplace,
        Boolean hasListings,
        StockFilter stock,
        String searchTerm,
        List<UUID> categoryIds
) {
}
