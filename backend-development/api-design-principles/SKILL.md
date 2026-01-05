---
name: api-design-principles
description: Master REST API design principles to build intuitive, scalable, and maintainable APIs. Applicable for designing new APIs, reviewing API specifications, or establishing API design standards in Java Spring projects.
---

# API Design Principles

Master REST API design principles to build intuitive, scalable, and maintainable APIs. Applicable for designing new APIs, reviewing API specifications, or establishing API design standards in Java Spring projects.

## When to Use This Skill

- Designing new REST APIs for Java Spring projects
- Refactoring existing APIs for better usability
- Establishing API design standards for your team
- Reviewing API specifications before implementation
- Creating developer-friendly API documentation
- Optimizing APIs for specific use cases (mobile, third-party integrations)

## Core Concepts

### 1. RESTful Design Principles

**Resource-Oriented Architecture**
- Resources are nouns (users, orders, products), not verbs
- Use HTTP methods for actions (GET, POST, PUT, PATCH, DELETE)
- URLs represent resource hierarchies
- Consistent naming conventions

**HTTP Methods Semantics:**
- `GET`: Retrieve resources (idempotent, safe)
- `POST`: Create new resources
- `PUT`: Replace entire resource (idempotent)
- `PATCH`: Partial resource updates
- `DELETE`: Remove resources (idempotent)

### 2. API Versioning Strategies

**URL Versioning:**
```
/api/v1/users
/api/v2/users
```

**Header Versioning:**
```
Accept: application/vnd.api+json; version=1
```

**Query Parameter Versioning:**
```
/api/users?version=1
```

## REST API Design Patterns

### Pattern 1: Resource Collection Design

```java
// Good: Resource-oriented endpoints
// URI Pattern: /api/v1/<domain>/<subdomain>/<domainobj>

GET    /api/v1/system/user/users              // List users (with pagination)
POST   /api/v1/system/user/users              // Create user
GET    /api/v1/system/user/users?id={id}      // Get specific user
PUT    /api/v1/system/user/users?id={id}      // Replace user
PATCH  /api/v1/system/user/users?id={id}      // Update user fields
DELETE /api/v1/system/user/users?id={id}      // Delete user

// Nested resources
GET    /api/v1/system/order/orders?userId={userId}  // Get user's orders
POST   /api/v1/system/order/orders                  // Create order for user

// Bad: Action-oriented endpoints (avoid)
POST   /api/v1/system/user/createUser
POST   /api/v1/system/user/getUserById
POST   /api/v1/system/user/deleteUser
```

### Pattern 2: Pagination and Filtering

```java
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
public class PaginationParams {
    @Min(1)
    private int page = 1;
    
    @Min(1)
    @Max(100)
    private int pageSize = 20;
}

@Data
public class FilterParams {
    private String status;
    private String createdAfter;
    private String search;
}

@Data
public class PaginatedResponse<T> {
    private int code;
    private String message;
    private PaginatedData<T> data;
    
    @Data
    public static class PaginatedData<T> {
        private List<T> dataList;
        private int total;
        private int size;
        private int pages;
        
        public boolean hasNext() {
            return size < pages;
        }
        
        public boolean hasPrev() {
            return size > 1;
        }
    }
}

// Spring MVC Controller example
@RestController
@RequestMapping("/api/v1/system/user")
public class UserController {

    @GetMapping("/users")
    public PaginatedResponse<UserDTO> listUsers(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        // Apply filters
        UserQuery query = buildQuery(status, search);
        
        // Count total
        int total = userService.countUsers(query);
        
        // Fetch page
        int offset = (page - 1) * pageSize;
        List<UserDTO> users = userService.fetchUsers(query, pageSize, offset);
        
        // Build response
        PaginatedResponse<UserDTO> response = new PaginatedResponse<>();
        response.setCode(0);
        response.setMessage("success");
        
        PaginatedResponse.PaginatedData<UserDTO> data = new PaginatedResponse.PaginatedData<>();
        data.setDataList(users);
        data.setTotal(total);
        data.setSize(page);
        data.setPages((total + pageSize - 1) / pageSize);
        
        response.setData(data);
        return response;
    }
}
```

### Pattern 3: Error Handling and Status Codes

```java
import lombok.Data;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private Object data;
}

@Data
@AllArgsConstructor
public class ValidationErrorDetail {
    private String field;
    private String message;
    private Object value;
}

// Error code constants based on goldcard specification
public class ErrorCodes {
    // Success
    public static final int SUCCESS = 0;
    
    // Reserved [1, 99999]
    public static final int UNKNOWN_ERROR = 99999;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int UNPROCESSABLE = 422;
    public static final int INTERNAL_ERROR = 500;
    
    // Infrastructure [100000, 199999]
    // Domain Digital Code * 1000 + Internal Domain Code
    public static final int INFRA_NET_ERROR = 101000;      // net (101)
    public static final int INFRA_DB_ERROR = 102000;       // db (102)
    public static final int INFRA_CACHE_ERROR = 103000;    // cache (103)
    public static final int INFRA_MQ_ERROR = 104000;       // mq (104)
    
    // Public [200000, 299999]
    public static final int COMM_AUTH_ERROR = 201000;      // auth (201)
    public static final int COMM_FLOW_ERROR = 202000;      // flow (202)
    public static final int COMM_NTF_ERROR = 203000;       // ntf (203)
    
    // Business Domain
    // IOT [300000, 399999]
    public static final int IOT_BASE_ERROR = 301000;
    
    // CIS [400000, 499999]
    public static final int CIS_BASE_ERROR = 401000;
}

// Custom exceptions
public class ResourceNotFoundException extends RuntimeException {
    private final String resource;
    private final String id;
    
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
        this.resource = resource;
        this.id = id;
    }
    
    public String getResource() { return resource; }
    public String getId() { return id; }
}

public class ValidationException extends RuntimeException {
    private final List<ValidationErrorDetail> errors;
    
    public ValidationException(List<ValidationErrorDetail> errors) {
        super("Validation failed");
        this.errors = errors;
    }
    
    public List<ValidationErrorDetail> getErrors() { return errors; }
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
            ErrorCodes.NOT_FOUND,
            ex.getMessage(),
            Map.of("resource", ex.getResource(), "id", ex.getId())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse response = new ErrorResponse(
            ErrorCodes.UNPROCESSABLE,
            "Request validation failed",
            Map.of("errors", ex.getErrors())
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse response = new ErrorResponse(
            ErrorCodes.INTERNAL_ERROR,
            "Internal server error",
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

// Example usage in controller
@RestController
@RequestMapping("/api/v1/system/user")
public class UserController {

    @GetMapping("/users")
    public ErrorResponse getUser(@RequestParam String id) {
        User user = userService.fetchUser(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", id);
        }
        return new ErrorResponse(ErrorCodes.SUCCESS, "success", user);
    }
}
```

### Pattern 4: HATEOAS (Hypermedia as the Engine of Application State)

```java
import lombok.Data;
import java.util.Map;
import java.util.HashMap;

@Data
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private Map<String, LinkInfo> links;
    
    @Data
    @AllArgsConstructor
    public static class LinkInfo {
        private String href;
        private String method;
        
        public LinkInfo(String href) {
            this.href = href;
            this.method = "GET";
        }
    }
    
    public static UserResponse fromUser(User user, String baseUrl) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        
        Map<String, LinkInfo> links = new HashMap<>();
        links.put("self", new LinkInfo(baseUrl + "/api/v1/system/user/users?id=" + user.getId()));
        links.put("orders", new LinkInfo(baseUrl + "/api/v1/system/order/orders?userId=" + user.getId()));
        links.put("update", new LinkInfo(
            baseUrl + "/api/v1/system/user/users?id=" + user.getId(), 
            "PATCH"
        ));
        links.put("delete", new LinkInfo(
            baseUrl + "/api/v1/system/user/users?id=" + user.getId(), 
            "DELETE"
        ));
        
        response.setLinks(links);
        return response;
    }
}

// Response wrapper following goldcard specification
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
```

## Best Practices

### REST APIs
1. **Consistent Naming**: Use plural nouns for collections (`/users`, not `/user`)
2. **Stateless**: Each request contains all necessary information
3. **Use HTTP Status Codes Correctly**: 2xx success, 4xx client errors, 5xx server errors
4. **Version Your API**: Plan for breaking changes from day one
5. **Pagination**: Always paginate large collections
6. **Rate Limiting**: Protect your API with rate limits
7. **Documentation**: Use OpenAPI/Swagger for interactive docs
8. **Path Design**: Follow `/api/v1/<domain>/<subdomain>/<domainobj>` pattern
9. **Response Format**: Use consistent `{code, message, data}` structure
10. **Error Codes**: Use domain-based error code segmentation

## Common Pitfalls

- **Over-fetching/Under-fetching**: Consider field selection or separate endpoints
- **Breaking Changes**: Version APIs or use deprecation strategies
- **Inconsistent Error Formats**: Standardize error responses with `{code, message, data}`
- **Missing Rate Limits**: APIs without limits are vulnerable to abuse
- **Poor Documentation**: Undocumented APIs frustrate developers
- **Ignoring HTTP Semantics**: POST for idempotent operations breaks expectations
- **Tight Coupling**: API structure shouldn't mirror database schema
- **ID in Path**: Avoid referencing `id` directly in path; use parameters instead

## Resources

- **references/rest-best-practices.md**: Comprehensive REST API design guide
- **assets/rest-api-template.java**: Spring MVC REST API template
- **assets/api-design-checklist.md**: Pre-implementation review checklist
