package id.ac.ui.cs.advprog.groupproject.catalog.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Template Method that encodes the idempotency-key pattern shared between the
 * stock decrease and product rating flows.
 *
 * <p>Concrete subclasses implement four steps:</p>
 * <ol>
 *   <li>{@link #isAlreadyProcessed(Object)} — fast soft-check using the idempotency key.</li>
 *   <li>{@link #recordEvent(Object)} — persist an event row whose key column has a {@code UNIQUE}
 *       constraint, so concurrent duplicate requests are rejected by the database.</li>
 *   <li>{@link #performAction(Object)} — the actual side-effecting work, executed only once per
 *       idempotency key.</li>
 *   <li>{@link #buildDuplicateResponse(Object)} — what to return when the operation has already
 *       been processed (either via the soft-check or the race-condition catch).</li>
 * </ol>
 *
 * <p>The {@link #onDuplicate(Object)} hook is called whenever a duplicate is detected, useful for
 * metrics / logging.</p>
 *
 * @param <K> idempotency-key type (typically {@link java.util.UUID})
 * @param <R> return type of the operation
 */
public abstract class IdempotentOperation<K, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentOperation.class);

    /**
     * Run the idempotent operation. This method is final to enforce the template structure.
     */
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

    /** Short, log-safe name used in info logs (e.g. {@code stock_decrease}). */
    protected abstract String operationName();

    /** Soft check whether an event with this key has already been processed. */
    protected abstract boolean isAlreadyProcessed(K idempotencyKey);

    /**
     * Persist the event entity. Must throw {@link DataIntegrityViolationException} (typically via
     * a {@code UNIQUE} constraint violation) when a concurrent request raced to insert first.
     */
    protected abstract void recordEvent(K idempotencyKey);

    /** Execute the actual side-effecting business action exactly once per idempotency key. */
    protected abstract R performAction(K idempotencyKey);

    /** Build a no-op response to return when the operation has already been processed. */
    protected abstract R buildDuplicateResponse(K idempotencyKey);

    /** Hook invoked on every duplicate detection (both soft-check and race-condition paths). */
    protected void onDuplicate(K idempotencyKey) {
        // optional override
    }
}
