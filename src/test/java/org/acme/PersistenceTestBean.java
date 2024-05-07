package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class PersistenceTestBean {

    private final AtomicInteger statusCommitedCounter = new AtomicInteger(0);
    private final AtomicInteger statusRolledBackCounter = new AtomicInteger(0);
    private final AtomicInteger statusOtherCounter = new AtomicInteger(0);

    @Inject
    EntityManager entityManager;

    @Inject
    Event<TestEvent> event;

    @Inject
    TransactionManager transactionManager;


    @Transactional
    public void createEntity(final int i) {
        final var transactionSynchronization = new TransactionSynchronization();
        try {
            transactionManager.getTransaction().registerSynchronization(transactionSynchronization);
        } catch (final SystemException | RollbackException e) {
            throw new RuntimeException("error registering transaction synchronization", e);
        }

        event.fire(new TestEvent("entity" + i));

        final var entity = new TestEntity();
        entity.setId("entity" + i);

        entityManager.persist(entity);
    }


    @Transactional
    public int getEntityCount() {
        final var query = entityManager.getCriteriaBuilder().createQuery(TestEntity.class);
        query.from(TestEntity.class);

        return entityManager.createQuery(query).getResultList().size();
    }


    @Transactional
    public void killConnections() {
        final var query = entityManager.createNativeQuery("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'mydb' AND pid <> pg_backend_pid();");
        query.getSingleResult();
    }


    public int getStatusCommitedCounter() {
        return statusCommitedCounter.get();
    }


    public int getStatusRolledBackCounter() {
        return statusRolledBackCounter.get();
    }


    public int getStatusOtherCounter() {
        return statusOtherCounter.get();
    }


    private class TransactionSynchronization implements Synchronization {


        @Override
        public void beforeCompletion() {
            // do nothing
        }


        @Override
        public void afterCompletion(final int status) {
            if (status == Status.STATUS_COMMITTED) {
                statusCommitedCounter.incrementAndGet();
            } else if (status == Status.STATUS_ROLLEDBACK) {
                statusRolledBackCounter.incrementAndGet();
            } else {
                statusOtherCounter.incrementAndGet();
            }
        }
    }

}
