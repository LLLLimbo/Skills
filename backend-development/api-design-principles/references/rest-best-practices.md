# REST API Best Practices

## URL Structure

### Resource Naming
```
# Good - Plural nouns with domain structure
# Pattern: /api/v1/<domain>/<subdomain>/<domainobj>
GET /api/v1/system/user/users
GET /api/v1/system/order/orders
GET /api/v1/system/product/products

# Bad - Verbs or mixed conventions
GET /api/v1/system/user/getUser
GET /api/v1/system/user/user  (inconsistent singular)
POST /api/v1/system/order/createOrder
```

### Path Design (Goldcard Specification)
```
# External Provision (For Third-Party Interface Calls)
/api/v1/<domain>/<subdomain>/<domainobj> + METHOD

# Frontend Provision
/api/<domain>/<subdomain>/<domainobj> + METHOD

# Internal Provision (Backend Service-to-Service Calls)
/inter/<domain>/<subdomain>/<domainobj> + METHOD

# Note: ID is passed as query parameter, not in path
GET /api/v1/system/user/users?id={id}
```

### Nested Resources
```
# Shallow nesting (preferred)
GET /api/v1/system/user/users?id={id}
GET /api/v1/system/order/orders?userId={userId}

# Deep nesting (avoid)
GET /api/v1/system/user/users/{id}/orders/{orderId}/items/{itemId}/reviews
# Better:
GET /api/v1/system/order/order-items?id={id}
GET /api/v1/system/review/reviews?orderItemId={id}
```

## HTTP Methods and Status Codes

### GET - Retrieve Resources
```
GET /api/v1/system/user/users              → 200 OK (with list)
GET /api/v1/system/user/users?id={id}      → 200 OK or 404 Not Found
GET /api/v1/system/user/users?page=2       → 200 OK (paginated)
```

### POST - Create Resources
```
POST /api/v1/system/user/users
  Body: {"name": "John", "email": "john@example.com"}
  → 201 Created
  Body: {"code": 0, "message": "success", "data": {"id": "123", "name": "John", ...}}

POST /api/v1/system/user/users (validation error)
  → 422 Unprocessable Entity
  Body: {"code": 422, "message": "Validation failed", "data": {"errors": [...]}}
```

### PUT - Replace Resources
```
PUT /api/v1/system/user/users?id={id}
  Body: {complete user object}
  → 200 OK (updated)
  → 404 Not Found (doesn't exist)

# Must include ALL fields
```

### PATCH - Partial Update
```
PATCH /api/v1/system/user/users?id={id}
  Body: {"name": "Jane"}  (only changed fields)
  → 200 OK
  → 404 Not Found
```

### DELETE - Remove Resources
```
DELETE /api/v1/system/user/users?id={id}
  → 204 No Content (deleted)
  → 404 Not Found
  → 409 Conflict (can't delete due to references)
```

## Response Format (Goldcard Specification)

### Single Object Response
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "123",
    "name": "John",
    "email": "john@example.com"
  }
}
```

### List Response
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "size": 1,
    "total": 100,
    "pages": 5,
    "dataList": [
      {
        "id": "123",
        "name": "John"
      }
    ]
  }
}
```

### Error Response
```json
{
  "code": 404,
  "message": "User not found",
  "data": {
    "resource": "User",
    "id": "999"
  }
}
```

## Error Code Specification (Goldcard Specification)

### Error Code Segmentation
| Segment             | Range              | Description                                           |
|---------------------|--------------------|-------------------------------------------------------|
| **Success**         | `0`                | Success                                               |
| **Reserved**        | `[1, 99999]`       | 99999 is for generic unknown errors                   |
| **Infrastructure**  | `[100000, 199999]` | Domain Digital Code * 1000 + Internal Domain Code     |
| **Public**          | `[200000, 299999]` | Domain Digital Code * 1000 + Internal Domain Code     |
| **Business Domain** | `[300000+]`        | Domain Digital Code * 1000 + Internal Domain Code     |

### Domain Classification
| Domain             | Domain Code | Domain Digital Code | Sub-items (Examples)                               |
|--------------------|-------------|---------------------|----------------------------------------------------|
| **Infrastructure** | `infra`     | `1`                 | `net` (101), `db` (102), `cache` (103), `mq` (104) |
| **Public**         | `comm`      | `2`                 | `auth` (201), `flow` (202), `ntf` (203)            |
| **IOT**            | `iot`       | `3`                 | `iot xxx` (301), `iot::yyy` (302)                  |
| **CIS**            | `cis`       | `4`                 | `cis XXX` (401), `cis: yyy` (402)                  |

## Filtering, Sorting, and Searching

### Query Parameters
```
# Filtering
GET /api/v1/system/user/users?status=active
GET /api/v1/system/user/users?role=admin&status=active

# Sorting
GET /api/v1/system/user/users?sort=createdAt
GET /api/v1/system/user/users?sort=-createdAt  (descending)
GET /api/v1/system/user/users?sort=name,createdAt

# Searching
GET /api/v1/system/user/users?search=john
GET /api/v1/system/user/users?q=john

# Field selection (sparse fieldsets)
GET /api/v1/system/user/users?fields=id,name,email
```

## Pagination Patterns

### Offset-Based Pagination
```java
GET /api/v1/system/user/users?page=2&pageSize=20

Response:
{
  "code": 0,
  "message": "success",
  "data": {
    "size": 2,
    "total": 150,
    "pages": 8,
    "dataList": [...]
  }
}
```

### Cursor-Based Pagination (for large datasets)
```java
GET /api/v1/system/user/users?limit=20&cursor=eyJpZCI6MTIzfQ

Response:
{
  "code": 0,
  "message": "success",
  "data": {
    "dataList": [...],
    "nextCursor": "eyJpZCI6MTQzfQ",
    "hasMore": true
  }
}
```

## Versioning Strategies

### URL Versioning (Recommended)
```
/api/v1/system/user/users
/api/v2/system/user/users

Pros: Clear, easy to route
Cons: Multiple URLs for same resource
```

### Header Versioning
```
GET /api/system/user/users
Accept: application/vnd.api+json; version=2

Pros: Clean URLs
Cons: Less visible, harder to test
```

### Query Parameter
```
GET /api/system/user/users?version=2

Pros: Easy to test
Cons: Optional parameter can be forgotten
```

## Rate Limiting

### Headers
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 742
X-RateLimit-Reset: 1640000000

Response when limited:
429 Too Many Requests
Retry-After: 3600
```

### Implementation Pattern
```java
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final int maxRequests;
    private final long windowMs;
    private final ConcurrentHashMap<String, RateLimitInfo> cache = new ConcurrentHashMap<>();
    
    public RateLimitInterceptor(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        
        RateLimitInfo info = cache.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                return new RateLimitInfo(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });
        
        if (info.count.get() > maxRequests) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(windowMs / 1000));
            return false;
        }
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(maxRequests - info.count.get()));
        return true;
    }
    
    private static class RateLimitInfo {
        long windowStart;
        AtomicInteger count;
        
        RateLimitInfo(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
```

## Authentication and Authorization

### Bearer Token
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

401 Unauthorized - Missing/invalid token
403 Forbidden - Valid token, insufficient permissions
```

### API Keys
```
X-API-Key: your-api-key-here
```

## Error Response Format

### Consistent Structure (Goldcard Specification)
```json
{
  "code": 422,
  "message": "Request validation failed",
  "data": {
    "errors": [
      {
        "field": "email",
        "message": "Invalid email format",
        "value": "not-an-email"
      }
    ]
  }
}
```

### Status Code Guidelines
- `200 OK`: Successful GET, PATCH, PUT
- `201 Created`: Successful POST
- `204 No Content`: Successful DELETE
- `400 Bad Request`: Malformed request
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Authenticated but not authorized
- `404 Not Found`: Resource doesn't exist
- `409 Conflict`: State conflict (duplicate email, etc.)
- `422 Unprocessable Entity`: Validation errors
- `429 Too Many Requests`: Rate limited
- `500 Internal Server Error`: Server error
- `503 Service Unavailable`: Temporary downtime

## Caching

### Cache Headers
```
# Client caching
Cache-Control: public, max-age=3600

# No caching
Cache-Control: no-cache, no-store, must-revalidate

# Conditional requests
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
If-None-Match: "33a64df551425fcc55e4d42a148795d9f25f89d4"
→ 304 Not Modified
```

## Bulk Operations

### Batch Endpoints
```java
POST /api/v1/system/user/users/batch
{
  "items": [
    {"name": "User1", "email": "user1@example.com"},
    {"name": "User2", "email": "user2@example.com"}
  ]
}

Response:
{
  "code": 0,
  "message": "success",
  "data": {
    "results": [
      {"id": "1", "status": "created"},
      {"id": null, "status": "failed", "error": "Email already exists"}
    ]
  }
}
```

## Idempotency

### Idempotency Keys
```
POST /api/v1/system/order/orders
Idempotency-Key: unique-key-123

If duplicate request:
→ 200 OK (return cached response)
```

## CORS Configuration

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://example.com")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

## Documentation with OpenAPI

```java
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@OpenAPIDefinition(
    info = @Info(
        title = "My API",
        description = "API for managing users",
        version = "1.0.0"
    )
)
@RestController
@RequestMapping("/api/v1/system/user")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    @Operation(
        summary = "Get user by ID",
        description = "Retrieve user by ID. Returns full user profile including basic information, contact details, and account status."
    )
    @GetMapping(value = "/users", params = "id")
    public ApiResponse<User> getUser(
        @Parameter(description = "The user ID") @RequestParam String id
    ) {
        // Implementation
        return null;
    }
}
```

## Health and Monitoring Endpoints

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        return ApiResponse.success(Map.of(
            "status", "healthy",
            "version", "1.0.0",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/health/detailed")
    public ApiResponse<Map<String, Object>> detailedHealth() {
        return ApiResponse.success(Map.of(
            "status", "healthy",
            "checks", Map.of(
                "database", checkDatabase(),
                "redis", checkRedis(),
                "externalApi", checkExternalApi()
            )
        ));
    }
    
    private String checkDatabase() {
        // Check database connection
        return "healthy";
    }
    
    private String checkRedis() {
        // Check Redis connection
        return "healthy";
    }
    
    private String checkExternalApi() {
        // Check external API
        return "healthy";
    }
}
```
