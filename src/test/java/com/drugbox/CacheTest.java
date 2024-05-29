package com.drugbox;

import com.drugbox.service.MapService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheTest {

    @Autowired
    MapService mapService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        cacheManager.getCache("addresses").clear();
        mapService.saveSeoulDrugBinLocations();
    }

    @Test
    public void 서울_주소_캐싱_테스트(){
        long start = System.currentTimeMillis();
        mapService.getSeoulDrugBinLocations();
        long executionTimeWithoutCache = System.currentTimeMillis() - start;
        System.out.println("Execution time without cache: " + executionTimeWithoutCache + "ms");

        start = System.currentTimeMillis();
        mapService.getSeoulDrugBinLocations();
        long executionTimeWithCache = System.currentTimeMillis() - start;
        System.out.println("Execution time with cache: " + executionTimeWithCache + "ms");

        assertTrue("Execution time with cache should be less than without cache",
                executionTimeWithoutCache > executionTimeWithCache);
    }
}
