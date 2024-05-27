package com.drugbox;

import com.drugbox.domain.BinId;
import com.drugbox.domain.BinLocation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class EmbeddedIdTest {

    @Autowired
    EntityManager em;


    @Test
    @Transactional
    void 복합키_테스트(){
        // save
        BinId binId = new BinId(1L, 1);
        BinLocation binLocation = new BinLocation(binId, "0", "0", "addr", null, null, null);
        em.persist(binLocation);

        // get
        BinId binId2 = new BinId(1L, 1);
        BinLocation findBinLocation = em.find(BinLocation.class, binId2);

        // validation
        Assertions.assertThat(binLocation).isEqualTo(findBinLocation);

    }
}
