/**
 * Production-ready REST API template using Spring MVC.
 * Includes pagination, filtering, error handling, and best practices.
 * Following goldcard specification for path design and response format.
 */

package com.example.api;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// ==================== Configuration ====================

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // CORS: Configures Cross-Origin Resource Sharing
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*") // TODO: Update this with specific origins in production
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(false); // TODO: Set to true if you need cookies/auth headers
    }
}

// ==================== Models ====================

enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class UserBase {
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    private UserStatus status = UserStatus.ACTIVE;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class UserCreate extends UserBase {
    @NotBlank
    @Size(min = 8)
    private String password;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class UserUpdate {
    @Email
    private String email;
    
    @Size(min = 1, max = 100)
    private String name;
    
    private UserStatus status;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class User extends UserBase {
    private String id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// ==================== Response Models (Goldcard Specification) ====================

/**
 * Standard API response format following goldcard specification.
 * Structure: {code, message, data}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCodes.SUCCESS, "success", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ErrorCodes.SUCCESS, message, data);
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}

/**
 * Paginated response data following goldcard specification.
 * Structure: {size, total, pages, dataList}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class PaginatedData<T> {
    private int size;      // Current page number
    private int total;     // Total data count
    private int pages;     // Total number of pages
    private List<T> dataList;
}

// ==================== Error Codes (Goldcard Specification) ====================

/**
 * Error code constants based on goldcard specification.
 * 
 * Segmentation:
 * - Success: 0
 * - Reserved: [1, 99999]
 * - Infrastructure: [100000, 199999]
 * - Public: [200000, 299999]
 * - Business Domain: [300000+]
 * 
 * Formula: Domain Digital Code * 1000 + Internal Domain Code
 */
class ErrorCodes {
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
    public static final int TOO_MANY_REQUESTS = 429;
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

// ==================== Exception Handling ====================

@Data
@AllArgsConstructor
class ValidationErrorDetail {
    private String field;
    private String message;
    private Object value;
}

class ResourceNotFoundException extends RuntimeException {
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

class ValidationException extends RuntimeException {
    private final List<ValidationErrorDetail> errors;
    
    public ValidationException(List<ValidationErrorDetail> errors) {
        super("Validation failed");
        this.errors = errors;
    }
    
    public List<ValidationErrorDetail> getErrors() { return errors; }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleNotFound(ResourceNotFoundException ex) {
        ApiResponse<Map<String, String>> response = ApiResponse.error(
            ErrorCodes.NOT_FOUND,
            ex.getMessage(),
            Map.of("resource", ex.getResource(), "id", ex.getId())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidation(ValidationException ex) {
        ApiResponse<Map<String, Object>> response = ApiResponse.error(
            ErrorCodes.UNPROCESSABLE,
            "Request validation failed",
            Map.of("errors", ex.getErrors())
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error(
            ErrorCodes.INTERNAL_ERROR,
            "Internal server error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

// ==================== Controller ====================

/**
 * User API Controller following goldcard specification.
 * 
 * Path Pattern: /api/v1/<domain>/<subdomain>/<domainobj>
 * - External: /api/v1/system/user/users
 * - Frontend: /api/system/user/users
 * - Internal: /inter/system/user/users
 * 
 * Note: ID is passed as query parameter, not in path.
 */
@RestController
@RequestMapping("/api/v1/system/user")
class UserController {

    /**
     * List users with pagination and filtering.
     * GET /api/v1/system/user/users
     */
    @GetMapping("/users")
    public ApiResponse<PaginatedData<User>> listUsers(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) String search
    ) {
        // Mock implementation
        int total = 100;
        int pages = (total + pageSize - 1) / pageSize;
        
        List<User> users = IntStream.range((page - 1) * pageSize, Math.min(page * pageSize, total))
                .mapToObj(i -> {
                    User user = new User();
                    user.setId(String.valueOf(i));
                    user.setEmail("user" + i + "@example.com");
                    user.setName("User " + i);
                    user.setStatus(UserStatus.ACTIVE);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    return user;
                })
                .collect(Collectors.toList());
        
        PaginatedData<User> data = new PaginatedData<>(page, total, pages, users);
        return ApiResponse.success(data);
    }

    /**
     * Create a new user.
     * POST /api/v1/system/user/users
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody UserCreate userCreate) {
        // Mock implementation
        User user = new User();
        user.setId("123");
        user.setEmail(userCreate.getEmail());
        user.setName(userCreate.getName());
        user.setStatus(userCreate.getStatus());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    /**
     * Get user by ID.
     * GET /api/v1/system/user/users?id={id}
     * 
     * Note: ID is passed as query parameter following goldcard specification.
     */
    @GetMapping(value = "/users", params = "id")
    public ApiResponse<User> getUser(@RequestParam String id) {
        // Mock: Check if exists
        if ("999".equals(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        
        User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setName("User Name");
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return ApiResponse.success(user);
    }

    /**
     * Partially update user.
     * PATCH /api/v1/system/user/users?id={id}
     */
    @PatchMapping(value = "/users", params = "id")
    public ApiResponse<User> updateUser(@RequestParam String id, @RequestBody UserUpdate update) {
        // Validate user exists
        if ("999".equals(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        
        // Mock: Apply updates
        User user = new User();
        user.setId(id);
        user.setEmail(update.getEmail() != null ? update.getEmail() : "user@example.com");
        user.setName(update.getName() != null ? update.getName() : "User Name");
        user.setStatus(update.getStatus() != null ? update.getStatus() : UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return ApiResponse.success(user);
    }

    /**
     * Delete user.
     * DELETE /api/v1/system/user/users?id={id}
     */
    @DeleteMapping(value = "/users", params = "id")
    public ResponseEntity<Void> deleteUser(@RequestParam String id) {
        // Validate user exists
        if ("999".equals(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        
        return ResponseEntity.noContent().build();
    }
}

// ==================== Health Check ====================

@RestController
class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        return ApiResponse.success(Map.of(
            "status", "healthy",
            "version", "1.0.0",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
