package com.pingyu.infodiet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InfoDietApplicationTests {

    @Test
    void applicationClassShouldExist() {
        assertNotNull(InfoDietApplication.class);
        assertEquals("com.pingyu.infodiet.InfoDietApplication", InfoDietApplication.class.getName());
    }
}
