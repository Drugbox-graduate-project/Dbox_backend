package com.drugbox.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugSaveRequest {
    @NotNull
    private Long drugboxId;
    @NotNull
    private String name;
    private String type;
    private List<DrugDetailSaveRequest> detail;

}
