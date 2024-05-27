package com.drugbox.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class BinId implements Serializable {
    @Column(name = "bin_id")
    private Long binId;
    @Column(name = "division_key")
    private Integer divisionKey;
}
