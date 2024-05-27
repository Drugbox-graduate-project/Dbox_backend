package com.drugbox.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DrugDetailResponse {
    private String name;
    private List<DrugResponse> drugResponses;
    private String effect;
}
