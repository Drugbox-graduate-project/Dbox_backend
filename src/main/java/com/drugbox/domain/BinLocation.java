package com.drugbox.domain;

import com.drugbox.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinLocation extends BaseEntity {

    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "binlocation_id")
    private BinId id;

    @Column(nullable = false)
    private String lat;
    @Column(nullable = false)
    private String lng;
    @Column(nullable = false)
    private String address;
    private String addrLvl1; // 시,도
    private String addrLvl2; // 시,군,구
    private String detail;
}
