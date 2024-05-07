package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@ApplicationScoped
class PersistenceTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int COUNT = 100000;

    @Inject
    PersistenceTestBean testBean;

    @Inject
    ObserverBean observerBean;


    @Test
    void testPersistence() {
        Thread thread = startKillThread();

        int exceptionCounter = 0;
        for (int i = 0; i < COUNT; i++) {
            try {
                logger.info("create entity {}", i);
                testBean.createEntity(i);
            } catch (Exception e) {
                logger.info("exception in iteration {}", i);
                exceptionCounter++;
            }
        }

        thread.interrupt();

        final var entityCount = testBean.getEntityCount();
        logger.info("""
                        \nCOUNT = {}; 
                        exceptionCounter = {}; 
                        COUNT - exceptionCounter = {}; 
                        entityCount = {};
                        observerBean.getAfterCompletionCount() = {};
                        observerBean.getAfterSuccessCount() = {};
                        observerBean.getAfterFailureCount() = {};
                        """,
                COUNT, exceptionCounter, COUNT - exceptionCounter, entityCount,
                observerBean.getAfterCompletionCount(), observerBean.getAfterSuccessCount(), observerBean.getAfterFailureCount());


        assertEquals(COUNT, observerBean.getAfterCompletionCount());
        assertEquals(COUNT, observerBean.getAfterSuccessCount() + observerBean.getAfterFailureCount());
        assertEquals(exceptionCounter, observerBean.getAfterFailureCount());

        assertEquals(observerBean.getAfterFailureCount(), testBean.getStatusRolledBackCounter());
        assertEquals(observerBean.getAfterSuccessCount(), testBean.getStatusCommitedCounter());
        assertEquals(0, testBean.getStatusOtherCounter());

        assertEquals(entityCount, observerBean.getAfterSuccessCount());
        assertEquals(COUNT - exceptionCounter, entityCount);
    }


    private @NotNull Thread startKillThread() {
        Runnable task = () -> {
            while (observerBean.getAfterCompletionCount() < COUNT) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    logger.info("kill connections");
                    testBean.killConnections();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("exception during kill of connections", e);
                }

            }
        };
        Thread thread = new Thread(task);
        thread.start();
        return thread;
    }


}
