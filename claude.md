# Claude.md

## Project overview
Jastip Online Nasional (JSON) is a jastip platform that connects travelers (Jastipers) and buyers (Titipers).
Core rules:
- Jastiper cannot buy their own product.
- Stock cannot go negative (anti-overselling).
- Wallet balance cannot go negative.
- Order status must follow sequence (Paid -> Purchased -> Shipped -> Completed) and can be Cancelled.
- Cancellation must trigger refund.

Modules (4 required):
- Auth & Profile
- Inventory & Catalog
- Order
- Wallet & Transactions
Optional module: Voucher & Promo (only required if 5 members).

## Tech stack
- Java 21
- Spring Boot 3.x
- Gradle (Kotlin DSL)
- JPA/Hibernate
- Flyway
- PostgreSQL/H2

## Run and test
Common commands:
- Build: ./gradlew build
- Test (all): ./gradlew test
- Run app: ./gradlew bootRun

Windows:
- Test: gradlew.bat test
- Run: gradlew.bat bootRun

Targeted test:
- ./gradlew test --tests "id.ac.ui.cs.advprog.groupproject.wallet.service.WalletServiceImplTest"

## AI contributor guide
- Follow package-by-feature structure.
- Use @Transactional for write operations that must be atomic.
- For wallet balance mutations, use row locking (pessimistic lock) to prevent race conditions.
- Refunds must be idempotent (referenceId required and unique).
- Validate inputs early; throw IllegalArgumentException for invalid requests.
- Keep commits small and focused.
- Commit message format: feat: / fix: (optional scope in parentheses).

## Milestone checklist
Use this section as a living checklist; update statuses as work progresses.

### 25%
General:
- [ ] Setup database
- [ ] Setup CD
- [ ] Test deployment
Auth:
- [ ] Bare auth (login)
Inventory:
- [ ] Basic CRUD
- [ ] Basic unit tests
Order:
- [ ] Init DB (Order and StatusHistory)
- [ ] Skeleton API endpoints for create and update status
Wallet:
- [ ] Init DB (Wallet and WalletTransaction)
- [ ] Auto create wallet on register
- [ ] Top-up basic
- [ ] Get balance
- [ ] Unit tests for top-up and get balance

### 50%
Auth:
- [ ] Role authorization
Inventory:
- [ ] Auto reduce stock
- [ ] Search by product/seller
- [ ] Profile integration
Order:
- [ ] Checkout flow (stock + wallet validation)
- [ ] Save new order (quantity + address)
- [ ] Validate initial status
- [ ] Unit tests for checkout success/fail
Wallet:
- [ ] Deduct balance on checkout
- [ ] Prevent negative balance
- [ ] Save DEBIT transaction
- [ ] Unit tests for deduct

### 75%
Auth:
- [ ] KYC approval flow
- [ ] Admin monitoring
- [ ] Safety features
Inventory:
- [ ] Locking anti-overselling
- [ ] Admin integration & monitoring
Order:
- [ ] Sequential status validation (Paid -> Purchased -> Shipped -> Completed)
- [ ] Cancel order + auto refund
- [ ] Rating system
- [ ] Order history endpoints (Titiper and Jastiper)
Wallet:
- [ ] Auto refund on cancel
- [ ] Concurrency validation (no double deduct)
- [ ] Integration testing for refund

### 100%
General:
- [ ] Full integration
- [ ] High test coverage
- [ ] Stress test war scenario
- [ ] Clean refactor + design pattern justification
Auth:
- [ ] Rating integration with Order
Inventory:
- [ ] 80% coverage and 0 Sonar issues
Order:
- [ ] Admin transaction log endpoint
- [ ] Testing for stock/payment consistency under load
- [ ] 90%+ coverage
Wallet:
- [ ] Edge case validation (amount <= 0, withdrawal > balance)
- [ ] Complete transaction history (timestamp, type, status)
- [ ] 90%+ coverage

## Notes
- Update this document if requirements change.
