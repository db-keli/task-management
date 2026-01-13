package org.example.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.enums.ModelType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IdCounterManagerTest {
    private IdCounterManager idManager;

    @BeforeEach
    public void setUp() {
        IdCounterManager.resetInstance();
        idManager = IdCounterManager.getInstance();
        idManager.resetAllCounters();
    }

    @AfterEach
    public void tearDown() {
        idManager.resetAllCounters();
    }

    @Test
    public void testGetInstance_Singleton() {
        IdCounterManager instance1 = IdCounterManager.getInstance();
        IdCounterManager instance2 = IdCounterManager.getInstance();
        
        assertSame(instance1, instance2);
    }

    @Test
    public void testGetNextId_Task() {
        String id1 = idManager.getNextId(ModelType.TASK);
        String id2 = idManager.getNextId(ModelType.TASK);
        String id3 = idManager.getNextId(ModelType.TASK);
        
        assertEquals("T001", id1);
        assertEquals("T002", id2);
        assertEquals("T003", id3);
    }

    @Test
    public void testGetNextId_Project() {
        String id1 = idManager.getNextId(ModelType.PROJECT);
        String id2 = idManager.getNextId(ModelType.PROJECT);
        
        assertEquals("P001", id1);
        assertEquals("P002", id2);
    }

    @Test
    public void testGetNextId_User() {
        String id1 = idManager.getNextId(ModelType.USER);
        String id2 = idManager.getNextId(ModelType.USER);
        
        assertEquals("U001", id1);
        assertEquals("U002", id2);
    }

    @Test
    public void testGetNextId_DifferentTypes() {
        String taskId = idManager.getNextId(ModelType.TASK);
        String projectId = idManager.getNextId(ModelType.PROJECT);
        String userId = idManager.getNextId(ModelType.USER);
        
        assertEquals("T001", taskId);
        assertEquals("P001", projectId);
        assertEquals("U001", userId);
        
        String taskId2 = idManager.getNextId(ModelType.TASK);
        assertEquals("T002", taskId2);
    }

    @Test
    public void testGetNextId_NullType() {
        assertThrows(IllegalArgumentException.class, () -> idManager.getNextId(null));
    }

    @Test
    public void testGetNextIntId() {
        int id1 = idManager.getNextIntId(ModelType.TASK);
        int id2 = idManager.getNextIntId(ModelType.TASK);
        int id3 = idManager.getNextIntId(ModelType.TASK);
        
        assertEquals(1, id1);
        assertEquals(2, id2);
        assertEquals(3, id3);
    }

    @Test
    public void testGetNextIntId_NullType() {
        assertThrows(IllegalArgumentException.class, () -> idManager.getNextIntId(null));
    }

    @Test
    public void testResetCounter() {
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.TASK);
        
        idManager.resetCounter(ModelType.TASK);
        
        String id = idManager.getNextId(ModelType.TASK);
        assertEquals("T001", id);
    }

    @Test
    public void testResetCounter_NullType() {
        assertThrows(IllegalArgumentException.class, () -> idManager.resetCounter(null));
    }

    @Test
    public void testResetAllCounters() {
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.PROJECT);
        idManager.getNextId(ModelType.USER);
        
        idManager.resetAllCounters();
        
        assertEquals("T001", idManager.getNextId(ModelType.TASK));
        assertEquals("P001", idManager.getNextId(ModelType.PROJECT));
        assertEquals("U001", idManager.getNextId(ModelType.USER));
    }

    @Test
    public void testGetCurrentCounter() {
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.TASK);
        
        int current = idManager.getCurrentCounter(ModelType.TASK);
        assertEquals(3, current);
    }

    @Test
    public void testGetCurrentCounter_NullType() {
        assertThrows(IllegalArgumentException.class, () -> idManager.getCurrentCounter(null));
    }

    @Test
    public void testSetCounter() {
        idManager.setCounter(ModelType.TASK, 100);
        
        String id = idManager.getNextId(ModelType.TASK);
        assertEquals("T100", id);
    }

    @Test
    public void testSetCounter_NullType() {
        assertThrows(IllegalArgumentException.class, () -> idManager.setCounter(null, 10));
    }

    @Test
    public void testSetCounter_NegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> idManager.setCounter(ModelType.TASK, -1));
    }

    @Test
    public void testSetCounter_Zero() {
        idManager.setCounter(ModelType.TASK, 0);
        String id = idManager.getNextId(ModelType.TASK);
        assertEquals("T000", id);
    }

    @Test
    public void testConcurrentIdGeneration() throws InterruptedException {
        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        Set<String> generatedIds = new HashSet<>();
        AtomicInteger duplicateCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    String id = idManager.getNextId(ModelType.TASK);
                    synchronized (generatedIds) {
                        if (!generatedIds.add(id)) {
                            duplicateCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown();
        endLatch.await();
        
        assertEquals(0, duplicateCount.get());
        assertEquals(threadCount, generatedIds.size());
    }

    @Test
    public void testConcurrentDifferentTypes() throws InterruptedException {
        int threadsPerType = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadsPerType * 3);
        
        Set<String> taskIds = new HashSet<>();
        Set<String> projectIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        
        for (int i = 0; i < threadsPerType; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    String id = idManager.getNextId(ModelType.TASK);
                    synchronized (taskIds) {
                        taskIds.add(id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
            
            new Thread(() -> {
                try {
                    startLatch.await();
                    String id = idManager.getNextId(ModelType.PROJECT);
                    synchronized (projectIds) {
                        projectIds.add(id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
            
            new Thread(() -> {
                try {
                    startLatch.await();
                    String id = idManager.getNextId(ModelType.USER);
                    synchronized (userIds) {
                        userIds.add(id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown();
        endLatch.await();
        
        assertEquals(threadsPerType, taskIds.size());
        assertEquals(threadsPerType, projectIds.size());
        assertEquals(threadsPerType, userIds.size());
    }

    @Test
    public void testIdFormat() {
        String taskId = idManager.getNextId(ModelType.TASK);
        String projectId = idManager.getNextId(ModelType.PROJECT);
        String userId = idManager.getNextId(ModelType.USER);
        
        assertTrue(taskId.matches("T\\d{3}"));
        assertTrue(projectId.matches("P\\d{3}"));
        assertTrue(userId.matches("U\\d{3}"));
    }

    @Test
    public void testLargeNumberGeneration() {
        idManager.setCounter(ModelType.TASK, 999);
        
        String id = idManager.getNextId(ModelType.TASK);
        assertEquals("T999", id);
        
        String nextId = idManager.getNextId(ModelType.TASK);
        assertTrue(nextId.startsWith("T"));
    }

    @Test
    public void testSequentialGeneration() {
        for (int i = 1; i <= 50; i++) {
            String expected = String.format("T%03d", i);
            String actual = idManager.getNextId(ModelType.TASK);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testResetBetweenGenerations() {
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.TASK);
        
        idManager.resetCounter(ModelType.TASK);
        
        idManager.getNextId(ModelType.TASK);
        int current = idManager.getCurrentCounter(ModelType.TASK);
        assertEquals(2, current);
    }

    @Test
    public void testIndependentCounters() {
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.TASK);
        idManager.getNextId(ModelType.TASK);
        
        idManager.getNextId(ModelType.PROJECT);
        
        assertEquals(4, idManager.getCurrentCounter(ModelType.TASK));
        assertEquals(2, idManager.getCurrentCounter(ModelType.PROJECT));
    }

    @Test
    public void testSetAndReset() {
        idManager.setCounter(ModelType.TASK, 50);
        assertEquals("T050", idManager.getNextId(ModelType.TASK));
        
        idManager.resetCounter(ModelType.TASK);
        assertEquals("T001", idManager.getNextId(ModelType.TASK));
    }
}
