# API Design Checklist

## Pre-Implementation Review

### Resource Design
- [ ] Resources are nouns, not verbs
- [ ] Plural names for collections
- [ ] Consistent naming across all endpoints
- [ ] Clear resource hierarchy (avoid deep nesting >2 levels)
- [ ] All CRUD operations properly mapped to HTTP methods
- [ ] Follow `/api/v1/<domain>/<subdomain>/<domainobj>` pattern

### HTTP Methods
- [ ] GET for retrieval (safe, idempotent)
- [ ] POST for creation
- [ ] PUT for full replacement (idempotent)
- [ ] PATCH for partial updates
- [ ] DELETE for removal (idempotent)

### Status Codes
- [ ] 200 OK for successful GET/PATCH/PUT
- [ ] 201 Created for POST
- [ ] 204 No Content for DELETE
- [ ] 400 Bad Request for malformed requests
- [ ] 401 Unauthorized for missing auth
- [ ] 403 Forbidden for insufficient permissions
- [ ] 404 Not Found for missing resources
- [ ] 422 Unprocessable Entity for validation errors
- [ ] 429 Too Many Requests for rate limiting
- [ ] 500 Internal Server Error for server issues

### Response Format
- [ ] Consistent `{code, message, data}` structure
- [ ] Single object response format followed
- [ ] List response format with `{size, total, pages, dataList}` followed
- [ ] Error responses use same structure

### Error Codes
- [ ] Success code: `0`
- [ ] Reserved codes: `[1, 99999]` (99999 for unknown errors)
- [ ] Infrastructure codes: `[100000, 199999]`
- [ ] Public codes: `[200000, 299999]`
- [ ] Business domain codes properly segmented
- [ ] Formula: `Domain Digital Code * 1000 + Internal Domain Code`

### Pagination
- [ ] All collection endpoints paginated
- [ ] Default page size defined (e.g., 20)
- [ ] Maximum page size enforced (e.g., 100)
- [ ] Pagination metadata included (total, pages, size, dataList)
- [ ] Offset-based pattern implemented

### Filtering & Sorting
- [ ] Query parameters for filtering
- [ ] Sort parameter supported
- [ ] Search parameter for full-text search
- [ ] Field selection supported (sparse fieldsets)

### Versioning
- [ ] URL versioning strategy used (`/api/v1/...`)
- [ ] Version included in all external endpoints
- [ ] Deprecation policy documented

### Path Design
- [ ] External APIs: `/api/v1/<domain>/<subdomain>/<domainobj>`
- [ ] Frontend APIs: `/api/<domain>/<subdomain>/<domainobj>`
- [ ] Internal APIs: `/inter/<domain>/<subdomain>/<domainobj>`
- [ ] ID not in path, use query parameters instead

### Error Handling
- [ ] Consistent error response format `{code, message, data}`
- [ ] Detailed error messages
- [ ] Field-level validation errors
- [ ] Domain-based error codes for client handling

### Authentication & Authorization
- [ ] Authentication method defined (Bearer token, API key)
- [ ] Authorization checks on all endpoints
- [ ] 401 vs 403 used correctly
- [ ] Token expiration handled

### Rate Limiting
- [ ] Rate limits defined per endpoint/user
- [ ] Rate limit headers included
- [ ] 429 status code for exceeded limits
- [ ] Retry-After header provided

### Documentation
- [ ] OpenAPI/Swagger spec generated
- [ ] All endpoints documented
- [ ] Request/response examples provided
- [ ] Error responses documented
- [ ] Authentication flow documented

### Testing
- [ ] Unit tests for business logic
- [ ] Integration tests for endpoints
- [ ] Error scenarios tested
- [ ] Edge cases covered
- [ ] Performance tests for heavy endpoints

### Security
- [ ] Input validation on all fields
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CORS configured correctly
- [ ] HTTPS enforced
- [ ] Sensitive data not in URLs
- [ ] No secrets in responses

### Performance
- [ ] Database queries optimized
- [ ] N+1 queries prevented
- [ ] Caching strategy defined
- [ ] Cache headers set appropriately
- [ ] Large responses paginated

### Monitoring
- [ ] Logging implemented
- [ ] Error tracking configured
- [ ] Performance metrics collected
- [ ] Health check endpoint available
- [ ] Alerts configured for errors
