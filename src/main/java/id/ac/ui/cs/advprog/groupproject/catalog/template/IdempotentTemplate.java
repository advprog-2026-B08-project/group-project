package id.ac.ui.cs.advprog.groupproject.catalog.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public abstract class IdempotentTemplate<K, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentTemplate.class);

    public final R execute(K idempotencyKey) {
        if (isAlreadyProcessed(idempotencyKey)) {
            LOGGER.info("{}_duplicate idempotencyKey={}", operationName(), idempotencyKey);
            onDuplicate(idempotencyKey);
            return buildDuplicateResponse(idempotencyKey);
        }

        try {
            recordEvent(idempotencyKey);
        } catch (DataIntegrityViolationException ex) {
            LOGGER.info("{}_duplicate_race idempotencyKey={}", operationName(), idempotencyKey);
            onDuplicate(idempotencyKey);
            return buildDuplicateResponse(idempotencyKey);
        }

        return performAction(idempotencyKey);
    }

    protected abstract String operationName();

    protected abstract boolean isAlreadyProcessed(K idempotencyKey);

    protected abstract void recordEvent(K idempotencyKey);

    protected abstract R performAction(K idempotencyKey);

    protected abstract R buildDuplicateResponse(K idempotencyKey);

    protected void onDuplicate(K idempotencyKey) {
    }
}
