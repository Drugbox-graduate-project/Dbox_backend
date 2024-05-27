package com.drugbox.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MapDetailResponse {
    private String locationName;
    private String locationId;
    private String locationPhotos;
    private String formattedAddress;
    private String currentOpeningHours;
}
