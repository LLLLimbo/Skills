---
name: microservices-patterns
description: Design microservices architectures with service boundaries, event-driven communication, and resilience patterns. Use when building distributed systems, decomposing monoliths, or implementing microservices.
---

# Microservices Patterns

Master microservices architecture patterns including service boundaries, inter-service communication, data management, and resilience patterns for building distributed systems.

## When to Use This Skill

- Decomposing monoliths into microservices
- Designing service boundaries and contracts
- Implementing inter-service communication
- Managing distributed data and transactions
- Building resilient distributed systems
- Implementing service discovery and load balancing
- Designing event-driven architectures

## Core Concepts

### 1. Service Decomposition Strategies

**By Business Capability**
- Organize services around business functions
- Each service owns its domain
- Example: OrderService, PaymentService, InventoryService

**By Subdomain (DDD)**
- Core domain, supporting subdomains
- Bounded contexts map to services
- Clear ownership and responsibility

**Strangler Fig Pattern**
- Gradually extract from monolith
- New functionality as microservices
- Proxy routes to old/new systems

### 2. Communication Patterns

**Synchronous (Request/Response)**
- REST APIs
- gRPC

**Asynchronous (Events/Messages)**
- Event streaming (Kafka)
- Message queues (RabbitMQ, SQS)
- Pub/Sub patterns

### 3. Data Management

**Database Per Service**
- Each service owns its data
- No shared databases
- Loose coupling

**Saga Pattern**
- Distributed transactions
- Compensating actions
- Eventual consistency

### 4. Resilience Patterns

**Circuit Breaker**
- Fail fast on repeated errors
- Prevent cascade failures

**Retry with Backoff**
- Transient fault handling
- Exponential backoff

**Bulkhead**
- Isolate resources
- Limit impact of failures

## Service Decomposition Patterns

### Pattern 1: By Business Capability

```java
// E-commerce example

// Order Service
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Handles order lifecycle.
     */
    @Transactional
    public Order createOrder(OrderRequest orderData) {
        Order order = Order.create(orderData);
        orderRepository.save(order);
        
        // Publish event for other services
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(order.getId())
            .customerId(order.getCustomerId())
            .items(order.getItems())
            .total(order.getTotal())
            .build();
        
        eventPublisher.publishEvent(event);
        
        return order;
    }
}

// Payment Service (separate service)
@Service
public class PaymentService {
    
    @Autowired
    private PaymentGateway paymentGateway;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Handles payment processing.
     */
    @Transactional
    public PaymentResult processPayment(PaymentRequest paymentRequest) {
        // Process payment
        PaymentResult result = paymentGateway.charge(
            paymentRequest.getAmount(),
            paymentRequest.getCustomerId()
        );
        
        if (result.isSuccess()) {
            PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId(paymentRequest.getOrderId())
                .transactionId(result.getTransactionId())
                .build();
            
            eventPublisher.publishEvent(event);
        }
        
        return result;
    }
}

// Inventory Service (separate service)
@Service
public class InventoryService {
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Handles inventory management.
     */
    @Transactional
    public ReservationResult reserveItems(String orderId, List<OrderItem> items) {
        // Check availability
        for (OrderItem item : items) {
            Integer available = inventoryRepository.getAvailable(item.getProductId());
            if (available < item.getQuantity()) {
                return ReservationResult.builder()
                    .success(false)
                    .error("Insufficient inventory for " + item.getProductId())
                    .build();
            }
        }
        
        // Reserve items
        Reservation reservation = createReservation(orderId, items);
        
        InventoryReservedEvent event = InventoryReservedEvent.builder()
            .orderId(orderId)
            .reservationId(reservation.getId())
            .build();
        
        eventPublisher.publishEvent(event);
        
        return ReservationResult.builder()
            .success(true)
            .reservation(reservation)
            .build();
    }
}
```

### Pattern 2: API Gateway

```java
@RestController
@RequestMapping("/api")
public class APIGatewayController {
    
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    @Autowired
    private InventoryServiceClient inventoryServiceClient;
    
    /**
     * API Gateway endpoint for creating orders.
     */
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest orderData) {
        try {
            // Route to order service
            Order order = orderServiceClient.createOrder(orderData);
            return ResponseEntity.ok(Map.of("order", order));
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Order service unavailable");
        }
    }
    
    /**
     * Aggregate data from multiple services.
     */
    @GetMapping("/orders/{orderId}/aggregate")
    public ResponseEntity<Map<String, Object>> getOrderAggregate(@PathVariable String orderId) {
        Map<String, Object> result = new HashMap<>();
        
        // Parallel requests using CompletableFuture
        CompletableFuture<Order> orderFuture = CompletableFuture
            .supplyAsync(() -> orderServiceClient.getOrder(orderId));
        
        CompletableFuture<Payment> paymentFuture = CompletableFuture
            .supplyAsync(() -> paymentServiceClient.getPaymentByOrderId(orderId))
            .exceptionally(ex -> null);
        
        CompletableFuture<Reservation> inventoryFuture = CompletableFuture
            .supplyAsync(() -> inventoryServiceClient.getReservationByOrderId(orderId))
            .exceptionally(ex -> null);
        
        // Wait for all futures
        CompletableFuture.allOf(orderFuture, paymentFuture, inventoryFuture).join();
        
        // Handle partial failures
        result.put("order", orderFuture.join());
        if (paymentFuture.join() != null) {
            result.put("payment", paymentFuture.join());
        }
        if (inventoryFuture.join() != null) {
            result.put("inventory", inventoryFuture.join());
        }
        
        return ResponseEntity.ok(result);
    }
}

/**
 * Feign client for Order Service with circuit breaker.
 */
@FeignClient(name = "order-service", url = "http://order-service:8000")
public interface OrderServiceClient {
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @PostMapping("/orders")
    Order createOrder(@RequestBody OrderRequest orderData);
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderFallback")
    @GetMapping("/orders/{orderId}")
    Order getOrder(@PathVariable String orderId);
    
    default Order createOrderFallback(OrderRequest orderData, Exception ex) {
        throw new ServiceUnavailableException("Order service unavailable");
    }
    
    default Order getOrderFallback(String orderId, Exception ex) {
        return null;
    }
}
```

## Communication Patterns

### Pattern 1: Synchronous REST Communication

```java
// Service A calls Service B
@Component
public class ServiceClient {
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    
    /**
     * HTTP client with retries and timeout.
     */
    public ServiceClient(@Value("${service.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
        
        // Configure RestTemplate with timeout
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        
        this.restTemplate = new RestTemplate(factory);
    }
    
    /**
     * GET with automatic retries.
     */
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    public <T> T get(String path, Class<T> responseType) {
        String url = baseUrl + path;
        ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Request failed with status: " + response.getStatusCode());
        }
        
        return response.getBody();
    }
    
    /**
     * POST request.
     */
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    public <T, R> R post(String path, T requestBody, Class<R> responseType) {
        String url = baseUrl + path;
        ResponseEntity<R> response = restTemplate.postForEntity(url, requestBody, responseType);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Request failed with status: " + response.getStatusCode());
        }
        
        return response.getBody();
    }
}

// Usage
@Service
public class OrderService {
    
    @Autowired
    private ServiceClient paymentClient;
    
    public PaymentResult processOrder(PaymentRequest paymentData) {
        return paymentClient.post("/payments", paymentData, PaymentResult.class);
    }
}
```

### Pattern 2: Asynchronous Event-Driven

```java
// Event-driven communication with Kafka
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DomainEvent {
    private String eventId;
    private String eventType;
    private String aggregateId;
    private LocalDateTime occurredAt;
    private Map<String, Object> data;
}

@Service
public class EventBus {
    
    @Autowired
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;
    
    /**
     * Publish event to Kafka topic.
     */
    public void publish(DomainEvent event) {
        String topic = event.getEventType();
        
        kafkaTemplate.send(topic, event.getAggregateId(), event)
            .addCallback(
                result -> System.out.println("Event published: " + event.getEventId()),
                ex -> System.err.println("Failed to publish event: " + ex.getMessage())
            );
    }
}

@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

// Order Service publishes event
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private EventBus eventBus;
    
    @Transactional
    public Order createOrder(OrderRequest orderData) {
        Order order = saveOrder(orderData);
        
        DomainEvent event = DomainEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("OrderCreated")
            .aggregateId(order.getId())
            .occurredAt(LocalDateTime.now())
            .data(Map.of(
                "order_id", order.getId(),
                "customer_id", order.getCustomerId(),
                "total", order.getTotal()
            ))
            .build();
        
        eventBus.publish(event);
        
        return order;
    }
}

// Inventory Service listens for OrderCreated
@Service
public class InventoryEventListener {
    
    @Autowired
    private InventoryService inventoryService;
    
    /**
     * React to order creation.
     */
    @KafkaListener(topics = "OrderCreated", groupId = "inventory-service")
    public void handleOrderCreated(DomainEvent event) {
        Map<String, Object> data = event.getData();
        String orderId = (String) data.get("order_id");
        List<OrderItem> items = (List<OrderItem>) data.get("items");
        
        // Reserve inventory
        inventoryService.reserveInventory(orderId, items);
    }
}

@Configuration
public class KafkaConsumerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ConsumerFactory<String, DomainEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

### Pattern 3: Saga Pattern (Distributed Transactions)

```java
// Saga orchestration for order fulfillment
@FunctionalInterface
interface SagaAction {
    StepResult execute(Map<String, Object> context);
}

@FunctionalInterface
interface SagaCompensation {
    void compensate(Map<String, Object> context);
}

@Data
@AllArgsConstructor
class SagaStep {
    private String name;
    private SagaAction action;
    private SagaCompensation compensation;
}

enum SagaStatus {
    PENDING,
    COMPLETED,
    COMPENSATING,
    FAILED
}

@Data
@Builder
class SagaResult {
    private SagaStatus status;
    private Map<String, Object> data;
    private String error;
}

@Data
@Builder
class StepResult {
    private boolean success;
    private Map<String, Object> data;
    private String error;
}

@Service
public class OrderFulfillmentSaga {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private PaymentService paymentService;
    
    private final List<SagaStep> steps;
    
    /**
     * Orchestrated saga for order fulfillment.
     */
    public OrderFulfillmentSaga() {
        this.steps = Arrays.asList(
            new SagaStep("create_order", this::createOrder, this::cancelOrder),
            new SagaStep("reserve_inventory", this::reserveInventory, this::releaseInventory),
            new SagaStep("process_payment", this::processPayment, this::refundPayment),
            new SagaStep("confirm_order", this::confirmOrder, this::cancelOrderConfirmation)
        );
    }
    
    /**
     * Execute saga steps.
     */
    public SagaResult execute(OrderRequest orderData) {
        List<SagaStep> completedSteps = new ArrayList<>();
        Map<String, Object> context = new HashMap<>();
        context.put("order_data", orderData);
        
        try {
            for (SagaStep step : steps) {
                // Execute step
                StepResult result = step.getAction().execute(context);
                
                if (!result.isSuccess()) {
                    // Compensate
                    compensate(completedSteps, context);
                    return SagaResult.builder()
                        .status(SagaStatus.FAILED)
                        .error(result.getError())
                        .build();
                }
                
                completedSteps.add(step);
                context.putAll(result.getData());
            }
            
            return SagaResult.builder()
                .status(SagaStatus.COMPLETED)
                .data(context)
                .build();
                
        } catch (Exception e) {
            // Compensate on error
            compensate(completedSteps, context);
            return SagaResult.builder()
                .status(SagaStatus.FAILED)
                .error(e.getMessage())
                .build();
        }
    }
    
    /**
     * Execute compensating actions in reverse order.
     */
    private void compensate(List<SagaStep> completedSteps, Map<String, Object> context) {
        Collections.reverse(completedSteps);
        for (SagaStep step : completedSteps) {
            try {
                step.getCompensation().compensate(context);
            } catch (Exception e) {
                // Log compensation failure
                System.err.println("Compensation failed for " + step.getName() + ": " + e.getMessage());
            }
        }
    }
    
    // Step implementations
    private StepResult createOrder(Map<String, Object> context) {
        OrderRequest orderData = (OrderRequest) context.get("order_data");
        Order order = orderService.create(orderData);
        return StepResult.builder()
            .success(true)
            .data(Map.of("order_id", order.getId()))
            .build();
    }
    
    private void cancelOrder(Map<String, Object> context) {
        String orderId = (String) context.get("order_id");
        orderService.cancel(orderId);
    }
    
    private StepResult reserveInventory(Map<String, Object> context) {
        String orderId = (String) context.get("order_id");
        OrderRequest orderData = (OrderRequest) context.get("order_data");
        
        ReservationResult result = inventoryService.reserve(orderId, orderData.getItems());
        
        return StepResult.builder()
            .success(result.isSuccess())
            .data(Map.of("reservation_id", result.getReservationId()))
            .error(result.getError())
            .build();
    }
    
    private void releaseInventory(Map<String, Object> context) {
        String reservationId = (String) context.get("reservation_id");
        inventoryService.release(reservationId);
    }
    
    private StepResult processPayment(Map<String, Object> context) {
        String orderId = (String) context.get("order_id");
        OrderRequest orderData = (OrderRequest) context.get("order_data");
        
        PaymentResult result = paymentService.charge(orderId, orderData.getTotal());
        
        return StepResult.builder()
            .success(result.isSuccess())
            .data(Map.of("transaction_id", result.getTransactionId()))
            .error(result.getError())
            .build();
    }
    
    private void refundPayment(Map<String, Object> context) {
        String transactionId = (String) context.get("transaction_id");
        paymentService.refund(transactionId);
    }
    
    private StepResult confirmOrder(Map<String, Object> context) {
        String orderId = (String) context.get("order_id");
        orderService.confirm(orderId);
        return StepResult.builder()
            .success(true)
            .data(Map.of())
            .build();
    }
    
    private void cancelOrderConfirmation(Map<String, Object> context) {
        String orderId = (String) context.get("order_id");
        orderService.cancelConfirmation(orderId);
    }
}
```

## Resilience Patterns

### Circuit Breaker Pattern

```java
// Circuit breaker using Resilience4j
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)                    // 50% failure rate
            .waitDurationInOpenState(Duration.ofSeconds(30))  // Recovery timeout
            .slidingWindowSize(10)                       // Number of calls to track
            .minimumNumberOfCalls(5)                     // Minimum calls before calculating rate
            .permittedNumberOfCallsInHalfOpenState(2)    // Success threshold in half-open
            .build();
        
        return CircuitBreakerRegistry.of(config);
    }
    
    @Bean
    public CircuitBreaker paymentServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("paymentService");
    }
}

@Service
public class PaymentServiceClient {
    
    @Autowired
    private CircuitBreaker circuitBreaker;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Call payment service with circuit breaker protection.
     */
    public PaymentResult processPayment(PaymentRequest paymentData) {
        // Wrap the call with circuit breaker
        Supplier<PaymentResult> supplier = CircuitBreaker
            .decorateSupplier(circuitBreaker, () -> callPaymentService(paymentData));
        
        try {
            return supplier.get();
        } catch (CallNotPermittedException e) {
            // Circuit breaker is open
            throw new ServiceUnavailableException("Payment service circuit breaker is open");
        }
    }
    
    private PaymentResult callPaymentService(PaymentRequest paymentData) {
        String url = "http://payment-service:8001/payments";
        ResponseEntity<PaymentResult> response = restTemplate.postForEntity(
            url, 
            paymentData, 
            PaymentResult.class
        );
        return response.getBody();
    }
}

// Alternative: Using annotations
@Service
public class OrderService {
    
    @Autowired
    private PaymentServiceClient paymentClient;
    
    /**
     * Process order with circuit breaker using annotation.
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public Order processOrder(OrderRequest orderData) {
        // Create order
        Order order = createOrder(orderData);
        
        // Process payment (protected by circuit breaker)
        PaymentResult payment = paymentClient.processPayment(
            new PaymentRequest(order.getId(), order.getTotal())
        );
        
        if (payment.isSuccess()) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
        
        return order;
    }
    
    /**
     * Fallback method when circuit breaker is open.
     */
    private Order paymentFallback(OrderRequest orderData, Exception ex) {
        // Handle fallback logic
        Order order = createOrder(orderData);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        
        // Log the failure
        System.err.println("Payment service unavailable: " + ex.getMessage());
        
        return order;
    }
}

// Circuit breaker event monitoring
@Component
public class CircuitBreakerEventListener {
    
    @Autowired
    public CircuitBreakerEventListener(CircuitBreakerRegistry registry) {
        registry.circuitBreaker("paymentService")
            .getEventPublisher()
            .onStateTransition(event -> {
                System.out.println("Circuit breaker state changed: " + 
                    event.getStateTransition());
            })
            .onError(event -> {
                System.err.println("Circuit breaker recorded error: " + 
                    event.getThrowable().getMessage());
            });
    }
}
```

## Resources

- **references/service-decomposition-guide.md**: Breaking down monoliths
- **references/communication-patterns.md**: Sync vs async patterns
- **references/saga-implementation.md**: Distributed transactions
- **assets/circuit-breaker.java**: Production circuit breaker
- **assets/event-bus-template.java**: Kafka event bus implementation
- **assets/api-gateway-template.java**: Complete API gateway

## Best Practices

1. **Service Boundaries**: Align with business capabilities
2. **Database Per Service**: No shared databases
3. **API Contracts**: Versioned, backward compatible
4. **Async When Possible**: Events over direct calls
5. **Circuit Breakers**: Fail fast on service failures
6. **Distributed Tracing**: Track requests across services
7. **Service Registry**: Dynamic service discovery
8. **Health Checks**: Liveness and readiness probes

## Common Pitfalls

- **Distributed Monolith**: Tightly coupled services
- **Chatty Services**: Too many inter-service calls
- **Shared Databases**: Tight coupling through data
- **No Circuit Breakers**: Cascade failures
- **Synchronous Everything**: Tight coupling, poor resilience
- **Premature Microservices**: Starting with microservices
- **Ignoring Network Failures**: Assuming reliable network
- **No Compensation Logic**: Can't undo failed transactions
