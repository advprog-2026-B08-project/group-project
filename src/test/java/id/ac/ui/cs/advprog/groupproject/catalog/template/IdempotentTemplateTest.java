package id.ac.ui.cs.advprog.groupproject.catalog.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class IdempotentTemplateTest {

    @Test
    void execute_firstCall_runsFullPipeline() {
        AtomicInteger recordCount = new AtomicInteger();
        AtomicInteger actionCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();
        UUID key = UUID.randomUUID();

        IdempotentTemplate<UUID, String> op = new IdempotentTemplate<>() {
            @Override
            protected String operationName() { return "test"; }
            @Override
            protected boolean isAlreadyProcessed(UUID k) { return false; }
            @Override
            protected void recordEvent(UUID k) { recordCount.incrementAndGet(); }
            @Override
            protected String performAction(UUID k) {
                actionCount.incrementAndGet();
                return "executed";
            }
            @Override
            protected String buildDuplicateResponse(UUID k) { return "duplicate"; }
            @Override
            protected void onDuplicate(UUID k) { duplicateCount.incrementAndGet(); }
        };

        String result = op.execute(key);

        assertEquals("executed", result);
        assertEquals(1, recordCount.get());
        assertEquals(1, actionCount.get());
        assertEquals(0, duplicateCount.get());
    }

    @Test
    void execute_softCheckDetectsDuplicate_skipsAction() {
        AtomicInteger recordCount = new AtomicInteger();
        AtomicInteger actionCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        IdempotentTemplate<UUID, String> op = new IdempotentTemplate<>() {
            @Override
            protected String operationName() { return "test"; }
            @Override
            protected boolean isAlreadyProcessed(UUID k) { return true; }
            @Override
            protected void recordEvent(UUID k) { recordCount.incrementAndGet(); }
            @Override
            protected String performAction(UUID k) {
                actionCount.incrementAndGet();
                return "executed";
            }
            @Override
            protected String buildDuplicateResponse(UUID k) { return "duplicate"; }
            @Override
            protected void onDuplicate(UUID k) { duplicateCount.incrementAndGet(); }
        };

        String result = op.execute(UUID.randomUUID());

        assertEquals("duplicate", result);
        assertEquals(0, recordCount.get());
        assertEquals(0, actionCount.get());
        assertEquals(1, duplicateCount.get());
    }

    @Test
    void execute_raceCondition_returnsDuplicateResponse() {
        AtomicInteger actionCount = new AtomicInteger();
        AtomicInteger duplicateCount = new AtomicInteger();

        IdempotentTemplate<UUID, String> op = new IdempotentTemplate<>() {
            @Override
            protected String operationName() { return "test"; }
            @Override
            protected boolean isAlreadyProcessed(UUID k) { return false; }
            @Override
            protected void recordEvent(UUID k) {
                throw new DataIntegrityViolationException("unique constraint");
            }
            @Override
            protected String performAction(UUID k) {
                actionCount.incrementAndGet();
                return "executed";
            }
            @Override
            protected String buildDuplicateResponse(UUID k) { return "duplicate"; }
            @Override
            protected void onDuplicate(UUID k) { duplicateCount.incrementAndGet(); }
        };

        String result = op.execute(UUID.randomUUID());

        assertEquals("duplicate", result);
        assertEquals(0, actionCount.get());
        assertEquals(1, duplicateCount.get());
    }

    @Test
    void execute_otherRuntimeException_propagates() {
        IdempotentTemplate<UUID, String> op = new IdempotentTemplate<>() {
            @Override
            protected String operationName() { return "test"; }
            @Override
            protected boolean isAlreadyProcessed(UUID k) { return false; }
            @Override
            protected void recordEvent(UUID k) {
                throw new IllegalStateException("boom");
            }
            @Override
            protected String performAction(UUID k) { return "executed"; }
            @Override
            protected String buildDuplicateResponse(UUID k) { return "duplicate"; }
        };

        assertThrows(IllegalStateException.class, () -> op.execute(UUID.randomUUID()));
    }
}
