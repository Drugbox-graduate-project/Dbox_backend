package com.drugbox.domain;

import com.drugbox.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDate;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drug extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drug_id")
    private Long id;

    private String name;
    private String type;
    private int count;
    private String location;
    private LocalDate expDate;
    @Builder.Default
    private boolean isInDisposalList = false;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "drugbox_id")
    @OnDelete(action = OnDeleteAction.CASCADE) // delete drugbox -> delete drugs
    private Drugbox drugbox;

    public void setCount(int count) {
        this.count = count;
    }

    public void addToDisposalList() {
        this.isInDisposalList = true;
    }

}
