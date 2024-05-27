package com.drugbox.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordRequest {
    private String latitude;
    private String longitude;
}

