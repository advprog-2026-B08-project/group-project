package id.ac.ui.cs.advprog.groupproject.catalog.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

public abstract class IdempotentTemplate<K, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentTemplate.class);

    public final R execute(K idempotencyKey) {
        if (isAlreadyProcessed(idempotencyKey)) {
            logDuplicate("soft_check", idempotencyKey);
            onDuplicate(idempotencyKey);
            return buildDuplicateResponse(idempotencyKey);
        }

        try {
            recordEvent(idempotencyKey);
        } catch (DataIntegrityViolationException ex) {
            logDuplicate("race", idempotencyKey);
            onDuplicate(idempotencyKey);
            return buildDuplicateResponse(idempotencyKey);
        }

        return performAction(idempotencyKey);
    }

    private void logDuplicate(String detectionPath, K idempotencyKey) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("idempotent_duplicate operation={} path={} key={}",
                    operationName(),
                    detectionPath,
                    sanitizeForLog(idempotencyKey));
        }
    }

    private static String sanitizeForLog(Object value) {
        if (value == null) {
            return "null";
        }
        return value.toString().replaceAll("[\\r\\n\\t]", "_");
    }

    protected abstract String operationName();

    protected abstract boolean isAlreadyProcessed(K idempotencyKey);

    protected abstract void recordEvent(K idempotencyKey);

    protected abstract R performAction(K idempotencyKey);

    protected abstract R buildDuplicateResponse(K idempotencyKey);

    protected void onDuplicate(K idempotencyKey) {
    }
}
