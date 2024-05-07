package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ObserverBean {


    private final AtomicInteger afterSuccessCount = new AtomicInteger(0);
    private final AtomicInteger afterFailureCount = new AtomicInteger(0);
    private final AtomicInteger afterCompletionCount = new AtomicInteger(0);


    public void afterSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) final TestEvent event) {
        afterSuccessCount.incrementAndGet();
    }


    public void afterFailure(@Observes(during = TransactionPhase.AFTER_FAILURE) final TestEvent event) {
        afterFailureCount.incrementAndGet();
    }


    public void afterCompletion(@Observes(during = TransactionPhase.AFTER_COMPLETION) final TestEvent event) {
        afterCompletionCount.incrementAndGet();
    }


    public int getAfterSuccessCount() {
        return afterSuccessCount.get();
    }


    public int getAfterFailureCount() {
        return afterFailureCount.get();
    }


    public int getAfterCompletionCount() {
        return afterCompletionCount.get();
    }
}
