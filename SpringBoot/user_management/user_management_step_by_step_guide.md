# User Management System - Step-by-Step Implementation Guide

This guide will help you build a user management system from scratch, adding features incrementally. Follow each step and ask to proceed to the next step after completing the current one.

---

## Prerequisites

Before starting, ensure you have:
- Java 17+ installed
- Spring Boot 3.x
- PostgreSQL database
- Maven or Gradle
- Basic understanding of Spring Boot

---

## Step 1: Basic Project Setup and Simple User Registration/Login

### Prompt for Step 1:

```
I want to create a simple user management system with Spring Boot. Please help me:

1. Create a basic Spring Boot project structure with:
   - User entity with fields: userId, firstName, lastName, email, password, isActive
   - UserRepository interface extending JpaRepository
   - UserService with basic CRUD operations
   - UserController with REST endpoints:
     - POST /api/users/register - Register a new user
     - POST /api/users/login - Simple login (just check email and password)
     - GET /api/users/{id} - Get user by ID
     - GET /api/users - Get all users

2. Create a simple UserDTO for request/response

3. Password should be stored as plain text for now (we'll add security later)

4. No authentication/authorization yet - just simple endpoints

5. Use PostgreSQL database with basic configuration

6. Keep exceptions minimal - just basic validation

Please create the project structure and basic implementation.
```

### What You'll Learn:
- Basic Spring Boot REST API setup
- JPA Entity and Repository
- Service layer pattern
- Controller endpoints
- DTO pattern

### Expected Endpoints:
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - Simple login check
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users` - Get all users

### After Completing Step 1:
Ask: "I've completed Step 1. Can we proceed to Step 2?"

---

## Step 2: Add Password Security (Hashing)

### Prompt for Step 2:

```
I've completed Step 1. Now I want to add password security:

1. Add password hashing using BCrypt or SHA-256
   - Hash password when registering
   - Hash password when updating
   - Compare hashed password during login

2. Update the login endpoint to:
   - Find user by email
   - Compare hashed password
   - Return success/failure message

3. Add password validation:
   - Minimum 6 characters
   - At least one uppercase, one lowercase, one number

4. Keep it simple - just basic password security

Please update the code to add password hashing.
```

### What You'll Learn:
- Password hashing with BCrypt
- Password validation
- Secure password storage

### After Completing Step 2:
Ask: "I've completed Step 2. Can we proceed to Step 3?"

---

## Step 3: Add CORS Configuration

### Prompt for Step 3:

```
I've completed Step 2. Now I need to add CORS support:

1. Create a CORS configuration class
   - Allow requests from frontend (e.g., http://localhost:3000)
   - Allow common HTTP methods (GET, POST, PUT, DELETE)
   - Allow common headers (Content-Type, Authorization)
   - Allow credentials

2. Configure CORS for all endpoints or specific paths

3. Keep it simple - just basic CORS setup

Please add CORS configuration to the project.
```

### What You'll Learn:
- CORS configuration in Spring Boot
- Cross-origin resource sharing
- Security configuration basics

### After Completing Step 3:
Ask: "I've completed Step 3. Can we proceed to Step 4?"

---

## Step 4: Add JWT Authentication

### Prompt for Step 4:

```
I've completed Step 3. Now I want to add JWT-based authentication:

1. Add JWT dependencies (io.jsonwebtoken)

2. Create JWT utility class with methods:
   - generateToken(userId, email) - Generate JWT token
   - validateToken(token) - Validate token
   - getUserIdFromToken(token) - Extract user ID from token
   - getEmailFromToken(token) - Extract email from token

3. Update login endpoint:
   - After successful login, generate JWT token
   - Return token in response

4. Create a simple authentication filter or interceptor:
   - Check for Authorization header
   - Validate JWT token
   - Extract user info from token

5. Add a protected endpoint example:
   - GET /api/users/profile - Get current user profile (requires token)

6. Keep it simple - basic JWT implementation

Please add JWT authentication to the project.
```

### What You'll Learn:
- JWT token generation and validation
- Token-based authentication
- Request filtering/interception

### After Completing Step 4:
Ask: "I've completed Step 4. Can we proceed to Step 5?"

---

## Step 5: Add Role-Based Authorization

### Prompt for Step 5:

```
I've completed Step 4. Now I want to add role-based authorization:

1. Create Role entity:
   - roleId, roleName, roleDescription

2. Create UserRole entity (many-to-many relationship):
   - User can have multiple roles
   - Role can be assigned to multiple users

3. Add default roles:
   - ADMIN
   - USER

4. Update User entity to include roles relationship

5. Create authorization check:
   - Add @PreAuthorize or custom annotation for role checking
   - Example: Only ADMIN can delete users

6. Add protected endpoints:
   - DELETE /api/users/{id} - Only ADMIN can delete
   - PUT /api/users/{id}/roles - Only ADMIN can assign roles
   - GET /api/users - All authenticated users can view

7. Keep it simple - basic role checking

Please add role-based authorization to the project.
```

### What You'll Learn:
- Role-based access control (RBAC)
- Many-to-many relationships in JPA
- Method-level security
- Authorization patterns

### After Completing Step 5:
Ask: "I've completed Step 5. Can we proceed to Step 6?"

---

## Step 6: Add Multi-Environment Configuration

### Prompt for Step 6:

```
I've completed Step 5. Now I want to add DYNAMIC support for multiple environments:

IMPORTANT: The environment configuration should be DYNAMIC - I should be able to add as many environments as I want with any names. For example:
- I might start with: dev1, dev2, dev3
- Later I might add: development4, qa1, qualityAssurance, staging, production, etc.
- The system should support ANY environment name I provide

1. Create a DYNAMIC environment configuration system:
   - Create application properties files for environments (examples: dev1, dev2, dev3, demo, main)
   - But make it easy to add NEW environments by just creating a new properties file
   - Pattern: application-{environment-name}.properties (where {environment-name} can be ANY name)

2. Each environment properties file should have:
   - Different database URL
   - Different database username/password
   - Different server port (optional)
   - Environment-specific JWT secret key
   - Any other environment-specific configuration

3. Create a base application.properties with common configuration that applies to all environments

4. Show how to run with different profiles dynamically:
   - Using spring.profiles.active={any-environment-name}
   - Or using environment variables
   - The system should work with ANY environment name, not just predefined ones

5. Make it clear that:
   - I can add new environments anytime by creating a new application-{name}.properties file
   - I can remove environments by deleting the properties file
   - No code changes needed when adding/removing environments

6. Keep it simple - just basic environment configuration but make it fully dynamic

Please add DYNAMIC multi-environment support to the project.
```

### What You'll Learn:
- Spring Boot profiles
- Environment-specific configuration
- Configuration management
- Dynamic environment setup (add/remove environments without code changes)

### After Completing Step 6:
Ask: "I've completed Step 6. Can we proceed to Step 7?"

---

## Step 7: Add Multi-Tenancy Support

### Prompt for Step 7:

```
I've completed Step 6. Now I want to add DYNAMIC multi-tenancy support:

IMPORTANT: The tenant system should be FULLY DYNAMIC - I should be able to add or remove tenants at runtime as an ADMIN. Tenants are NOT fixed.

1. Create Tenant entity with:
   - tenantId (auto-generated)
   - tenantName (unique, can be any name like "A", "B", "CompanyABC", "TenantXYZ", etc.)
   - companyName (display name for the company/organization)
   - schemaName (optional, for database schema isolation)
   - isActive (to enable/disable tenants)
   - createdAt, updatedAt

2. Create UserTenantMapping entity:
   - Links users to tenants (many-to-many relationship)
   - userId, tenantId
   - A user can belong to multiple tenants
   - A tenant can have multiple users

3. Add tenant context management:
   - Create TenantContext class to store current tenant in thread-local
   - Extract tenant from request header (e.g., X-Tenant-Id or X-Tenant-Name)
   - Or extract from JWT token (store tenant info in token)
   - Validate that tenant exists and is active

4. Update User entity to support tenant filtering:
   - When querying users, automatically filter by current tenant from context
   - When creating users, assign to current tenant from context
   - Ensure data isolation between tenants

5. Add DYNAMIC tenant management endpoints (all ADMIN only):
   - GET /api/tenants - Get all tenants (with pagination)
   - GET /api/tenants/{id} - Get tenant by ID
   - POST /api/tenants - Create NEW tenant dynamically (any name allowed)
   - PUT /api/tenants/{id} - Update tenant (change name, company name, etc.)
   - DELETE /api/tenants/{id} - Delete/Remove tenant (soft delete or hard delete)
   - PUT /api/tenants/{id}/activate - Activate tenant
   - PUT /api/tenants/{id}/deactivate - Deactivate tenant
   - GET /api/tenants/{id}/users - Get all users in a tenant

6. Add user-tenant assignment endpoints (ADMIN only):
   - POST /api/users/{userId}/tenants/{tenantId} - Assign user to tenant
   - DELETE /api/users/{userId}/tenants/{tenantId} - Remove user from tenant
   - GET /api/users/{userId}/tenants - Get all tenants for a user

7. Initial setup:
   - Create a few example tenants (A, B, C, D) as seed data
   - But make it clear these are just examples - admin can add/remove any tenants

8. Important requirements:
   - ADMIN can create tenants with ANY name (not limited to A, B, C, D)
   - ADMIN can remove any tenant (with proper validation - check if tenant has users)
   - Tenant names should be unique
   - When a tenant is deleted, handle user-tenant mappings appropriately
   - All tenant operations should be logged

9. Keep it simple - basic tenant isolation but fully dynamic management

Please add DYNAMIC multi-tenancy support to the project where tenants can be added/removed by ADMIN at runtime.
```

### What You'll Learn:
- Multi-tenancy architecture
- Tenant isolation
- Context management
- Data filtering by tenant
- Dynamic tenant management (CRUD operations for tenants)
- Runtime tenant addition/removal

### After Completing Step 7:
Ask: "I've completed Step 7. Can we proceed to Step 8?"

---

## Step 8: Add User Profile and Preferences

### Prompt for Step 8:

```
I've completed Step 7. Now I want to add user profile and preferences:

1. Extend User entity with:
   - profileImage
   - mobileNo
   - designation
   - reportingTo
   - address (create Address entity)

2. Create UserPreferences entity:
   - dateFormat
   - language
   - timezone
   - theme

3. Add endpoints:
   - PUT /api/users/{id}/profile - Update user profile
   - GET /api/users/{id}/profile - Get user profile
   - PUT /api/users/{id}/preferences - Update preferences
   - GET /api/users/{id}/preferences - Get preferences

4. Add profile image upload:
   - POST /api/users/{id}/profile-image
   - Store image as base64 or file path

5. Keep it simple - basic profile management

Please add user profile and preferences to the project.
```

### What You'll Learn:
- Entity relationships (One-to-One)
- File/image handling
- Profile management

### After Completing Step 8:
Ask: "I've completed Step 8. Can we proceed to Step 9?"

---

## Step 9: Add Password Reset Functionality

### Prompt for Step 9:

```
I've completed Step 8. Now I want to add password reset functionality:

1. Create PasswordResetToken entity:
   - token (unique)
   - userId
   - expiryTime
   - isUsed

2. Add endpoints:
   - POST /api/users/forgot-password - Generate reset token and send email (mock email for now)
   - POST /api/users/reset-password - Reset password using token
   - POST /api/users/change-password - Change password (requires current password)

3. Token generation:
   - Generate random token
   - Set expiry (e.g., 1 hour)
   - Store in database

4. Password reset flow:
   - User requests reset → Generate token → Return token (or mock email)
   - User uses token → Validate token → Reset password

5. Keep it simple - basic password reset

Please add password reset functionality to the project.
```

### What You'll Learn:
- Token-based password reset
- Token expiry management
- Password change flow

### After Completing Step 9:
Ask: "I've completed Step 9. Can we proceed to Step 10?"

---

## Step 10: Add User Activation/Deactivation

### Prompt for Step 10:

```
I've completed Step 9. Now I want to add user activation/deactivation:

1. Update User entity:
   - isActive field (already exists, but add logic)

2. Add endpoints:
   - PUT /api/users/{id}/activate - Activate user (ADMIN only)
   - PUT /api/users/{id}/deactivate - Deactivate user (ADMIN only)
   - PUT /api/users/activate-deactivate - Bulk activate/deactivate (ADMIN only)

3. Add validation:
   - Inactive users cannot login
   - Admin users cannot be deactivated
   - Check isActive during login

4. Add filtering:
   - GET /api/users?status=active - Get only active users
   - GET /api/users?status=inactive - Get only inactive users
   - GET /api/users?status=all - Get all users

5. Keep it simple - basic activation/deactivation

Please add user activation/deactivation to the project.
```

### What You'll Learn:
- User status management
- Bulk operations
- Query filtering

### After Completing Step 10:
Ask: "I've completed Step 10. Can we proceed to Step 11?"

---

## Step 11: Add User Groups

### Prompt for Step 11:

```
I've completed Step 10. Now I want to add user groups:

1. Create UserGroup entity:
   - groupId, groupName, description, isActive

2. Update User entity:
   - Add groupId (many-to-one relationship)

3. Add endpoints:
   - POST /api/groups - Create group (ADMIN only)
   - GET /api/groups - Get all groups
   - GET /api/groups/{id} - Get group by ID
   - PUT /api/groups/{id} - Update group (ADMIN only)
   - DELETE /api/groups/{id} - Delete group (ADMIN only)
   - PUT /api/users/assign-group - Assign users to group (ADMIN only)
   - GET /api/groups/{id}/users - Get users in group

4. Keep it simple - basic group management

Please add user groups to the project.
```

### What You'll Learn:
- Group management
- Many-to-one relationships
- Bulk user assignment

### After Completing Step 11:
Ask: "I've completed Step 11. Can we proceed to Step 12?"

---

## Step 12: Add Pagination and Filtering

### Prompt for Step 12:

```
I've completed Step 11. Now I want to add pagination and filtering:

1. Add pagination to GET endpoints:
   - GET /api/users?page=0&size=10&sort=firstName,asc
   - GET /api/groups?page=0&size=10

2. Add filtering:
   - GET /api/users?search=john - Search by name or email
   - GET /api/users?status=active&search=john - Combined filters

3. Create PagedResponse DTO:
   - content (list of items)
   - pageNumber
   - pageSize
   - totalElements
   - totalPages

4. Use Spring Data JPA Pageable

5. Keep it simple - basic pagination

Please add pagination and filtering to the project.
```

### What You'll Learn:
- Spring Data pagination
- Query filtering
- Response pagination structure

### After Completing Step 12:
Ask: "I've completed Step 12. Can we proceed to Step 13?"

---

## Step 13: Add Input Validation

### Prompt for Step 13:

```
I've completed Step 12. Now I want to add input validation:

1. Add validation annotations to DTOs:
   - @NotNull, @NotBlank for required fields
   - @Email for email fields
   - @Size for string length
   - @Pattern for custom validation (mobile number, etc.)

2. Add @Valid annotation to controller methods

3. Create custom validators if needed:
   - Mobile number format
   - Password strength

4. Return proper error messages for validation failures

5. Keep it simple - basic validation

Please add input validation to the project.
```

### What You'll Learn:
- Bean validation
- Custom validators
- Error handling

### After Completing Step 13:
Ask: "I've completed Step 13. Can we proceed to Step 14?"

---

## Step 14: Add Error Handling

### Prompt for Step 14:

```
I've completed Step 13. Now I want to add proper error handling:

1. Create custom exceptions:
   - UserNotFoundException
   - InvalidCredentialsException
   - DuplicateEmailException
   - InvalidTokenException

2. Create GlobalExceptionHandler:
   - Handle validation errors
   - Handle custom exceptions
   - Return consistent error response format

3. Error response format:
   - timestamp
   - status
   - error
   - message
   - path

4. Keep it simple - basic error handling

Please add error handling to the project.
```

### What You'll Learn:
- Exception handling
- Global exception handlers
- Consistent error responses

### After Completing Step 14:
Ask: "I've completed Step 14. Can we proceed to Step 15?"

---

## Step 15: Add Logging

### Prompt for Step 15:

```
I've completed Step 14. Now I want to add logging:

1. Add logging to service methods:
   - Log user creation
   - Log login attempts
   - Log errors
   - Log important operations

2. Use SLF4J with Logback

3. Configure log levels:
   - INFO for general operations
   - WARN for warnings
   - ERROR for errors
   - DEBUG for debugging (optional)

4. Log format should include:
   - Timestamp
   - Level
   - Class/Method
   - Message

5. Keep it simple - basic logging

Please add logging to the project.
```

### What You'll Learn:
- Logging best practices
- Logback configuration
- Log levels

### After Completing Step 15:
Ask: "I've completed Step 15. Can we proceed to Step 16?"

---

## Step 16: Add API Documentation (Swagger/OpenAPI)

### Prompt for Step 16:

```
I've completed Step 15. Now I want to add API documentation:

1. Add Swagger/OpenAPI dependencies

2. Configure Swagger:
   - API title, description, version
   - Add security scheme for JWT
   - Configure endpoints

3. Add annotations to controllers:
   - @Operation for endpoint descriptions
   - @ApiResponse for response codes
   - @Parameter for parameters

4. Access Swagger UI at /swagger-ui.html

5. Keep it simple - basic Swagger setup

Please add API documentation to the project.
```

### What You'll Learn:
- API documentation
- Swagger/OpenAPI
- API testing interface

### After Completing Step 16:
Ask: "I've completed Step 16. Can we proceed to Step 17?"

---

## Step 17: Add Database Migrations (Flyway/Liquibase)

### Prompt for Step 17:

```
I've completed Step 16. Now I want to add database migrations:

1. Add Flyway or Liquibase dependency

2. Create migration scripts:
   - V1__create_users_table.sql
   - V2__create_roles_table.sql
   - V3__create_user_roles_table.sql
   - V4__create_tenants_table.sql
   - etc.

3. Configure Flyway in application.properties

4. Migrations should run automatically on startup

5. Keep it simple - basic migration setup

Please add database migrations to the project.
```

### What You'll Learn:
- Database versioning
- Migration tools
- Schema management

### After Completing Step 17:
Ask: "I've completed Step 17. Can we proceed to Step 18?"

---

## Step 18: Add Unit Tests

### Prompt for Step 18:

```
I've completed Step 17. Now I want to add unit tests:

1. Create test classes for:
   - UserService tests
   - UserController tests (with MockMvc)

2. Use JUnit 5 and Mockito

3. Test scenarios:
   - User registration
   - User login
   - Password validation
   - Role assignment
   - Error cases

4. Use @SpringBootTest for integration tests

5. Keep it simple - basic test coverage

Please add unit tests to the project.
```

### What You'll Learn:
- Unit testing
- Integration testing
- Mocking
- Test coverage

### After Completing Step 18:
Ask: "I've completed Step 18. The implementation is complete!"

---

## Summary

You've built a complete user management system with:

✅ User registration and login
✅ Password security (hashing)
✅ CORS configuration
✅ JWT authentication
✅ Role-based authorization
✅ Dynamic multi-environment support (add any number of environments with any names)
✅ Dynamic multi-tenancy (add/remove tenants at runtime as ADMIN)
✅ User profiles and preferences
✅ Password reset
✅ User activation/deactivation
✅ User groups
✅ Pagination and filtering
✅ Input validation
✅ Error handling
✅ Logging
✅ API documentation
✅ Database migrations
✅ Unit tests

---

## Next Steps (Optional Enhancements)

After completing all steps, you can add:
- Email service integration
- File upload for profile images
- Advanced search functionality
- Audit logging
- Rate limiting
- Two-factor authentication (2FA)
- OAuth2 integration
- Kafka event publishing (as mentioned in your original system)

---

## How to Use This Guide

1. Start with Step 1
2. Copy the prompt for Step 1 and ask your AI assistant
3. Implement the code
4. Test the implementation
5. Once satisfied, ask to proceed to Step 2
6. Repeat for each step

## Tips

- Don't skip steps - each builds on the previous one
- Test after each step
- Keep code simple - avoid over-engineering
- Ask questions if something is unclear
- Review the code after each step

---

**Good luck with your implementation! 🚀**

