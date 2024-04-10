package com.drugbox.domain;

import com.drugbox.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drugbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drugbox_id")
    private Long id;

    @OneToMany(mappedBy = "drugbox", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserDrugbox> userDrugboxes = new ArrayList<>();

    private String name;
    private String image;
    private String inviteCode;

    public static Drugbox createDrugbox(String name, String image){
        String inviteCode = UUID.randomUUID().toString();
        Drugbox drugbox = Drugbox.builder()
                .name(name)
                .image(image)
                .inviteCode(inviteCode)
                .build();
        return drugbox;
    }

    public void setName(String name) {this.name = name; }
    public void setImage(String image) {this.image = image;}
}
