package org.example.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FunctionalUtilsTest {

    @Test
    public void testFunctionalUtilsExists() {
        FuctionalUtils functionalUtils = new FuctionalUtils();
        assertNotNull(functionalUtils);
    }

    @Test
    public void testFunctionalUtilsInstantiation() {
        assertDoesNotThrow(() -> new FuctionalUtils());
    }
}
