package org.example.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class StreamServiceTest {

    @Test
    public void testStreamServiceExists() {
        StreamService streamService = new StreamService();
        assertNotNull(streamService);
    }

    @Test
    public void testStreamServiceInstantiation() {
        assertDoesNotThrow(() -> new StreamService());
    }
}
