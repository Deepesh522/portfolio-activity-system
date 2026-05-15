# Portfolio System - Tech Notes

### Quick Start Using Docker

```bash
docker compose up --build
```

The gateway will be up on `localhost:8080`.

If you're debugging in your IDE, just spin up the infra first:

1. `docker compose up postgres rabbitmq -d`
2. Run the services using `./gradlew bootRun` in each module.

---

### How it's built (Architecture)

The system is built as a set of event-driven microservices. The main goal was to keep the core transaction logic (Portfolio Service) completely separate from the "noisy" work of sending notifications.

- **Async via RabbitMQ**: We use a Topic Exchange. When a trade happens, the Portfolio service just fires an event and forgets about it. The Notification service picks it up whenever it can. This prevents a slow Email API from breaking the user's trade experience.
- **DB Separation**: Each service has its own DB. No shared tables. We use Flyway for migrations so the schema versioning stays sane.
- **Design Patterns**:
  - **Strategies**: Used for the notification rules (High Value, Stock Watch, etc).
  - **Factory**: Used to resolve how the message actually gets delivered (Email vs SMS).
    This makes it easy to add new alert types without touching the core service code.
- **Tracing**: Every request gets an `X-Correlation-Id`. It travels from the Gateway through RabbitMQ and into the final logs, so we can actually follow a single trade across 3 different systems.

---

### Tradeoffs & Shortcuts

Building a "perfect" system takes forever, so I made some intentional choices here:

- **Gradle**: Since the project have multiple modules, gradle is a good choice for dependency management and build automation. It is also more flexible, though it also have a learning curve.
- **RabbitMQ**: I used rabbitMq because it's simpler to maintain for now and it is more reliable than others like redis pub/sub.
- **PostgreSQL**: Any rdbms (ACID compliant) with flyway support would work here, like mysql or oracle. But postgres have easier setup than oracle.
- **Project Loom (Virtual Threads)**: I went with Java 21's virtual threads. They are great for I/O heavy stuff like this, and it's cleaner than managing complex thread pools.
- **First-Match Rules**: Right now, the system stops at the first rule that matches an event. It keeps things fast, but eventually, we might want to support multiple alerts per trade.
- **Flyway**: This is used for schema migrations. It is a simple and very effective way to manage database schema changes over time.Though it is not as feature-rich as Liquibase.
- **Postgres instance**: I'm using one Postgres container with multiple databases for simplicity in local dev. In a real production environment, these would be separate RDS instances.

---

### Scalability

- **Horizontal scaling**: Since the services are stateless, you can just spin up more containers. RabbitMQ will handle the load balancing between them automatically.
- **Backpressure**: If the system gets hammered with trades, RabbitMQ acts as a buffer. The Notification service won't crash; it'll just work through the queue as fast as it can.
- **Loom Efficiency**: Because we use Virtual Threads, the memory overhead per request is tiny compared to a traditional Spring app. We can handle way more concurrent traffic on smaller hardware.

### Why virtual threads matters?

Traditional Java threads take about 1MB of stack memory each. Virtual threads are only a few hundred bytes.

- Old Java: 1,000 threads = 1GB RAM used.
- This Project: 1,000 threads = ~1MB RAM used.
