---
name: architecture-patterns
description: Implement proven backend architecture patterns including Clean Architecture, Hexagonal Architecture, and Domain-Driven Design. Use when architecting complex backend systems or refactoring existing applications for better maintainability.
---

# Architecture Patterns

Master proven backend architecture patterns including Clean Architecture, Hexagonal Architecture, and Domain-Driven Design to build maintainable, testable, and scalable systems.

## When to Use This Skill

- Designing new backend systems from scratch
- Refactoring monolithic applications for better maintainability
- Establishing architecture standards for your team
- Migrating from tightly coupled to loosely coupled architectures
- Implementing domain-driven design principles
- Creating testable and mockable codebases
- Planning microservices decomposition

## Core Concepts

### 1. Clean Architecture (Uncle Bob)

**Layers (dependency flows inward):**
- **Entities**: Core business models
- **Use Cases**: Application business rules
- **Interface Adapters**: Controllers, presenters, gateways
- **Frameworks & Drivers**: UI, database, external services

**Key Principles:**
- Dependencies point inward
- Inner layers know nothing about outer layers
- Business logic independent of frameworks
- Testable without UI, database, or external services

### 2. Hexagonal Architecture (Ports and Adapters)

**Components:**
- **Domain Core**: Business logic
- **Ports**: Interfaces defining interactions
- **Adapters**: Implementations of ports (database, REST, message queue)

**Benefits:**
- Swap implementations easily (mock for testing)
- Technology-agnostic core
- Clear separation of concerns

### 3. Domain-Driven Design (DDD)

**Strategic Patterns:**
- **Bounded Contexts**: Separate models for different domains
- **Context Mapping**: How contexts relate
- **Ubiquitous Language**: Shared terminology

**Tactical Patterns:**
- **Entities**: Objects with identity
- **Value Objects**: Immutable objects defined by attributes
- **Aggregates**: Consistency boundaries
- **Repositories**: Data access abstraction
- **Domain Events**: Things that happened

## Clean Architecture Pattern

### Directory Structure
```
src/main/java/com/example/
├── domain/                    # Entities & business rules
│   ├── entity/
│   │   ├── User.java
│   │   └── Order.java
│   ├── valueobject/
│   │   ├── Email.java
│   │   └── Money.java
│   └── repository/            # Abstract interfaces
│       ├── UserRepository.java
│       └── PaymentGateway.java
├── application/               # Application business rules (Use Cases)
│   ├── usecase/
│   │   ├── CreateUserUseCase.java
│   │   ├── ProcessOrderUseCase.java
│   │   └── SendNotificationUseCase.java
│   └── dto/
│       ├── CreateUserRequest.java
│       └── CreateUserResponse.java
├── adapter/                   # Interface implementations
│   ├── persistence/
│   │   ├── JpaUserRepository.java
│   │   └── RedisCacheRepository.java
│   ├── web/
│   │   └── UserController.java
│   └── gateway/
│       ├── StripePaymentGateway.java
│       └── SendGridEmailGateway.java
└── infrastructure/            # Framework & external concerns
    ├── config/
    │   ├── DatabaseConfig.java
    │   └── ApplicationConfig.java
    └── logging/
        └── LoggingAspect.java
```

### Implementation Example

```java
// domain/entity/User.java
package com.example.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core user entity - no framework dependencies.
 */
public class User {
    private final String id;
    private String email;
    private String name;
    private final LocalDateTime createdAt;
    private boolean active;

    public User(String id, String email, String name, LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
        this.active = active;
    }

    public static User create(String email, String name) {
        return new User(
            UUID.randomUUID().toString(),
            email,
            name,
            LocalDateTime.now(),
            true
        );
    }

    /** Business rule: deactivating user. */
    public void deactivate() {
        this.active = false;
    }

    /** Business rule: active users can order. */
    public boolean canPlaceOrder() {
        return this.active;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
}

// domain/repository/UserRepository.java
package com.example.domain.repository;

import com.example.domain.entity.User;
import java.util.Optional;

/**
 * Port: defines contract, no implementation.
 */
public interface UserRepository {
    Optional<User> findById(String userId);
    Optional<User> findByEmail(String email);
    User save(User user);
    boolean delete(String userId);
}

// application/dto/CreateUserRequest.java
package com.example.application.dto;

public record CreateUserRequest(String email, String name) {}

// application/dto/CreateUserResponse.java
package com.example.application.dto;

import com.example.domain.entity.User;

public record CreateUserResponse(User user, boolean success, String error) {
    public static CreateUserResponse success(User user) {
        return new CreateUserResponse(user, true, null);
    }

    public static CreateUserResponse failure(String error) {
        return new CreateUserResponse(null, false, error);
    }
}

// application/usecase/CreateUserUseCase.java
package com.example.application.usecase;

import com.example.application.dto.CreateUserRequest;
import com.example.application.dto.CreateUserResponse;
import com.example.domain.entity.User;
import com.example.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: orchestrates business logic.
 */
@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public CreateUserResponse execute(CreateUserRequest request) {
        // Business validation
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return CreateUserResponse.failure("Email already exists");
        }

        // Create entity
        User user = User.create(request.email(), request.name());

        // Persist
        User savedUser = userRepository.save(user);

        return CreateUserResponse.success(savedUser);
    }
}

// adapter/persistence/JpaUserRepository.java
package com.example.adapter.persistence;

import com.example.domain.entity.User;
import com.example.domain.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter: PostgreSQL/JPA implementation.
 */
@Repository
public class JpaUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JpaUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
        rs.getString("id"),
        rs.getString("email"),
        rs.getString("name"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getBoolean("is_active")
    );

    @Override
    public Optional<User> findById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId)
            .stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, userRowMapper, email)
            .stream().findFirst();
    }

    @Override
    public User save(User user) {
        String sql = """
            INSERT INTO users (id, email, name, created_at, is_active)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE
            SET email = ?, name = ?, is_active = ?
            """;
        jdbcTemplate.update(sql,
            user.getId(), user.getEmail(), user.getName(),
            user.getCreatedAt(), user.isActive(),
            user.getEmail(), user.getName(), user.isActive()
        );
        return user;
    }

    @Override
    public boolean delete(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId);
        return rowsAffected == 1;
    }
}

// adapter/web/UserController.java
package com.example.adapter.web;

import com.example.application.dto.CreateUserRequest;
import com.example.application.dto.CreateUserResponse;
import com.example.application.usecase.CreateUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller: handles HTTP concerns only.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        CreateUserResponse response = createUserUseCase.execute(request);

        if (!response.success()) {
            return ResponseEntity.badRequest().body(response.error());
        }

        return ResponseEntity.ok(response.user());
    }
}
```

## Hexagonal Architecture Pattern

```java
// Core domain (hexagon center)
package com.example.domain.service;

import com.example.domain.entity.Order;
import com.example.domain.port.OrderRepositoryPort;
import com.example.domain.port.PaymentGatewayPort;
import com.example.domain.port.NotificationPort;
import com.example.domain.valueobject.OrderResult;
import com.example.domain.valueobject.PaymentResult;
import org.springframework.stereotype.Service;

/**
 * Domain service - no infrastructure dependencies.
 */
@Service
public class OrderService {

    private final OrderRepositoryPort orders;
    private final PaymentGatewayPort payments;
    private final NotificationPort notifications;

    public OrderService(
            OrderRepositoryPort orders,
            PaymentGatewayPort payments,
            NotificationPort notifications) {
        this.orders = orders;
        this.payments = payments;
        this.notifications = notifications;
    }

    public OrderResult placeOrder(Order order) {
        // Business logic
        if (!order.isValid()) {
            return OrderResult.failure("Invalid order");
        }

        // Use ports (interfaces)
        PaymentResult payment = payments.charge(
            order.getTotal(),
            order.getCustomerId()
        );

        if (!payment.isSuccess()) {
            return OrderResult.failure("Payment failed");
        }

        order.markAsPaid();
        Order savedOrder = orders.save(order);

        notifications.send(
            order.getCustomerEmail(),
            "Order confirmed",
            "Order " + order.getId() + " confirmed"
        );

        return OrderResult.success(savedOrder);
    }
}

// Ports (interfaces)
package com.example.domain.port;

import com.example.domain.entity.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
    Order findById(String orderId);
}

package com.example.domain.port;

import com.example.domain.valueobject.Money;
import com.example.domain.valueobject.PaymentResult;

public interface PaymentGatewayPort {
    PaymentResult charge(Money amount, String customerId);
}

package com.example.domain.port;

public interface NotificationPort {
    void send(String to, String subject, String body);
}

// Adapters (implementations)
package com.example.adapter.gateway;

import com.example.domain.port.PaymentGatewayPort;
import com.example.domain.valueobject.Money;
import com.example.domain.valueobject.PaymentResult;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.exception.CardException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Primary adapter: connects to Stripe API.
 */
@Component
public class StripePaymentAdapter implements PaymentGatewayPort {

    public StripePaymentAdapter(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    @Override
    public PaymentResult charge(Money amount, String customerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount.getCents());
            params.put("currency", amount.getCurrency());
            params.put("customer", customerId);

            Charge charge = Charge.create(params);
            return PaymentResult.success(charge.getId());
        } catch (CardException e) {
            return PaymentResult.failure(e.getMessage());
        } catch (Exception e) {
            return PaymentResult.failure("Payment processing error");
        }
    }
}

/**
 * Test adapter: no external dependencies.
 */
@Component
public class MockPaymentAdapter implements PaymentGatewayPort {

    @Override
    public PaymentResult charge(Money amount, String customerId) {
        return PaymentResult.success("mock-123");
    }
}
```

## Domain-Driven Design Pattern

```java
// Value Objects (immutable)
package com.example.domain.valueobject;

import java.util.Objects;

/**
 * Value object: validated email.
 */
public final class Email {
    private final String value;

    public Email(String value) {
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        this.value = value;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}

/**
 * Value object: amount with currency.
 */
public final class Money {
    private final int amount;  // cents
    private final String currency;

    public Money(int amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount + other.amount, this.currency);
    }

    public int getAmount() { return amount; }
    public int getCents() { return amount; }
    public String getCurrency() { return currency; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount == money.amount && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() { return Objects.hash(amount, currency); }
}

// Entities (with identity)
package com.example.domain.entity;

import com.example.domain.valueobject.Money;
import com.example.domain.event.DomainEvent;
import com.example.domain.event.ItemAddedEvent;
import com.example.domain.event.OrderSubmittedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entity: has identity, mutable state.
 */
public class Order {
    private final String id;
    private final Customer customer;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.PENDING;
    private final List<DomainEvent> events = new ArrayList<>();

    public Order(String id, Customer customer) {
        this.id = id;
        this.customer = customer;
    }

    /** Business logic in entity. */
    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(product, quantity);
        items.add(item);
        events.add(new ItemAddedEvent(this.id, item));
    }

    /** Calculated property. */
    public Money getTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(new Money(0, "USD"), Money::add);
    }

    /** State transition with business rules. */
    public void submit() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot submit empty order");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order already submitted");
        }

        this.status = OrderStatus.SUBMITTED;
        events.add(new OrderSubmittedEvent(this.id));
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public List<DomainEvent> getEvents() { return Collections.unmodifiableList(events); }
    public void clearEvents() { events.clear(); }
}

// Aggregates (consistency boundary)
package com.example.domain.entity;

import com.example.domain.valueobject.Email;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Aggregate root: controls access to entities.
 */
public class Customer {
    private final String id;
    private final Email email;
    private final List<Address> addresses = new ArrayList<>();
    private final List<String> orderIds = new ArrayList<>();  // Order IDs, not full objects

    public Customer(String id, Email email) {
        this.id = id;
        this.email = email;
    }

    /** Aggregate enforces invariants. */
    public void addAddress(Address address) {
        if (addresses.size() >= 5) {
            throw new IllegalStateException("Maximum 5 addresses allowed");
        }
        addresses.add(address);
    }

    public Optional<Address> getPrimaryAddress() {
        return addresses.stream()
            .filter(Address::isPrimary)
            .findFirst();
    }

    public String getId() { return id; }
    public Email getEmail() { return email; }
}

// Domain Events
package com.example.domain.event;

import java.time.LocalDateTime;

public class OrderSubmittedEvent implements DomainEvent {
    private final String orderId;
    private final LocalDateTime occurredAt;

    public OrderSubmittedEvent(String orderId) {
        this.orderId = orderId;
        this.occurredAt = LocalDateTime.now();
    }

    public String getOrderId() { return orderId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}

// Repository (aggregate persistence)
package com.example.domain.repository;

import com.example.domain.entity.Order;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository: persist/retrieve aggregates.
 */
@Repository
public class OrderRepository {

    private final JpaOrderRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderRepository(JpaOrderRepository jpaRepository, 
                          ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    /** Reconstitute aggregate from storage. */
    public Optional<Order> findById(String orderId) {
        return jpaRepository.findById(orderId);
    }

    /** Persist aggregate and publish events. */
    public Order save(Order order) {
        Order savedOrder = jpaRepository.save(order);
        
        // Publish domain events
        order.getEvents().forEach(eventPublisher::publishEvent);
        order.clearEvents();
        
        return savedOrder;
    }
}
```

## Resources

- **references/clean-architecture-guide.md**: Detailed layer breakdown
- **references/hexagonal-architecture-guide.md**: Ports and adapters patterns
- **references/ddd-tactical-patterns.md**: Entities, value objects, aggregates
- **assets/clean-architecture-template/**: Complete project structure
- **assets/ddd-examples/**: Domain modeling examples

## Best Practices

1. **Dependency Rule**: Dependencies always point inward
2. **Interface Segregation**: Small, focused interfaces
3. **Business Logic in Domain**: Keep frameworks out of core
4. **Test Independence**: Core testable without infrastructure
5. **Bounded Contexts**: Clear domain boundaries
6. **Ubiquitous Language**: Consistent terminology
7. **Thin Controllers**: Delegate to use cases
8. **Rich Domain Models**: Behavior with data

## Common Pitfalls

- **Anemic Domain**: Entities with only data, no behavior
- **Framework Coupling**: Business logic depends on frameworks
- **Fat Controllers**: Business logic in controllers
- **Repository Leakage**: Exposing ORM objects
- **Missing Abstractions**: Concrete dependencies in core
- **Over-Engineering**: Clean architecture for simple CRUD