# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**News Service** is a Spring Boot microservice built with Gradle and Java 25 that handles file processing operations (news and discounts). It uses a **hexagonal architecture** with separate modules for different business domains, PostgreSQL for persistence, Redis for caching, and integrates with Eureka service discovery and OAuth2/Keycloak for security.

## Development Commands

### Build and Compile

```bash
# Build JAR without tests
./gradlew bootJar --no-daemon -x test

# Full build with tests
./gradlew build

# Clean build
./gradlew clean bootJar

# Format code with Spotless (Google Java Format)
./gradlew spotlessApply
```

### Running the Application

```bash
# Run with dev profile (loads from .env file)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run with prod profile
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew test --tests=*ArchivoTest

# Run a specific test class
./gradlew test --tests=NewsServiceApplicationTests
```

### Code Quality

```bash
# Run Checkstyle validation (configured in config/checkstyle/checkstyle.xml)
./gradlew checkstyleMain

# Format Java code with Google Java Format
./gradlew spotlessApply

# Check code formatting without applying changes
./gradlew spotlessCheck
```

### Database

```bash
# Flyway migrations run automatically on application startup
# Migrations are located in src/main/resources/db/migrations/
# Database config in src/main/resources/application-dev.properties
```

## Architecture Overview

### Hexagonal Architecture Pattern

The codebase is organized around **business domains** (modules), each implementing hexagonal architecture with clear separation of concerns:

```
[domain-name]/
├── application/        # Use cases and business logic
│   ├── service/       # Application services implementing use cases
│   ├── command/       # Command objects (for file processing)
│   └── mapper/        # DTO to domain model conversions
├── domain/            # Core business logic
│   ├── model/         # Domain entities and value objects
│   ├── port/          # Interface definitions
│   │   ├── input/     # Input ports (use case interfaces)
│   │   └── output/    # Output ports (repository/adapter interfaces)
│   └── exceptions/    # Domain-specific exceptions
└── infrastructure/    # Technical implementation
    ├── input/         # REST controllers and DTOs
    ├── output/        # Repository implementations and adapters
    ├── config/        # Domain-specific Spring configurations
    ├── client/        # External service clients (Feign)
    └── entity/        # JPA entity mappings
```

### Key Business Domains

1. **archivo** - File upload, validation, and processing (Excel, CSV)
2. **descuentos** - Discount management and processing
3. **common** - Shared configurations, utilities, and exceptions

### Cross-Cutting Concerns (common module)

- `config/AsyncConfig.java` - Async processing thread pools
- `config/SecurityConfig.java` - OAuth2/Keycloak integration
- `config/FileUploadConfig.java` - File upload constraints
- `config/DotenvConfig.java` - Environment variable loading
- `exceptions/` - Global exception handling

## Technology Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 25
- **Build Tool**: Gradle 9.1.0
- **Database**: PostgreSQL with Flyway migrations
- **Cache**: Redis
- **Messaging/Discovery**: Eureka Client, Spring Cloud
- **Security**: OAuth2 Resource Server (Keycloak)
- **File Processing**: Apache POI (Excel), OpenCSV (CSV)
- **Reactive**: Spring WebFlux, Project Reactor
- **HTTP Client**: Apache HttpClient 5, OpenFeign
- **Resilience**: Resilience4j (circuit breaking)
- **Code Quality**: Checkstyle, Spotless (Google Java Format)

## Development Environment Setup

### Required Services (Dev Profile)

Configure in `src/main/resources/application-dev.properties`:

- **PostgreSQL**: `localhost:5437` (database: `news_db`)
- **Redis**: `localhost:6379`
- **Eureka**: `localhost:8761` (for service discovery)
- **Keycloak**: `localhost:8181` (for OAuth2 JWT validation)

### Environment Variables

Create a `.env` file in the project root for dev:

```
DB_USERNAME_DEV=postgres
DB_PASSWORD_DEV=your_password
ENVIRONMENT=development
```

The application automatically loads `.env` when running with dev profile or in local environment.

### Key Application Properties

- **Server Port**: 0 (random port, registered with Eureka)
- **File Upload Limits**: 50MB max file size, 50MB max request
- **Allowed File Extensions**: xlsx, xls, csv
- **Async Processing**: 5 core threads, 10 max threads for file processing
- **Batch Processing**: 1000 item batches with 5 concurrent threads

## Important Patterns and Conventions

### Port and Adapter Pattern

- **Input Ports** (`domain/port/input/`): Define use case interfaces
  - `LeerArchivoUseCase`, `CargarArchivoUseCase`
- **Output Ports** (`domain/port/output/`): Define repository interfaces
  - `ArchivoDescuentosRepository`, `ArchivoNovedadesRepository`
- **Adapters** (`infrastructure/`): Implement ports
  - REST controllers (input adapters)
  - JPA repositories (output adapters)

### Dependency Injection

- Constructor injection is preferred (all services use constructor-based DI)
- Services are annotated with `@Service` and implement port interfaces
- Repositories extend Spring Data JPA `JpaRepository`

### Exception Handling

- Domain-specific exceptions in `domain/exceptions/`
  - Example: `ArchivoNoEncontradoException`, `ArchivoNoValidoException`
- Global exception handling through Spring's exception handler mechanism
- Use checked and unchecked exceptions appropriately

### File Processing

- **LeerArchivoService**: Reads Excel/CSV files into row arrays
- **CargarArchivoService**: Uploads files with validation
- Async processing through `@Async` methods and task executors
- Batch processing splits large files into 1000-item chunks

## CI/CD Pipeline

**GitHub Actions Workflow** (`.github/workflows/news-service.yml`):

1. **Build Job**: Compiles and creates JAR (excludes tests)
2. **Security Scan Job**: Runs Trivy vulnerability scanner
3. **Docker Build Job**: Builds and pushes image to GHCR
4. **Deploy Job**: SSH deploys to production VPS via Docker Compose

Triggers: Push to `develop` or `main` branches, or manual dispatch

## Deployment

### Docker Deployment

- **Dockerfile**: Multi-stage build optimized for Spring Boot
- **Image Registry**: GitHub Container Registry (ghcr.io)
- **VPS Deployment**: SSH-based deployment with Docker Compose
- **Health Checks**: Endpoint: `/actuator/health`

### Database Migrations

- **Tool**: Flyway 11.14.1
- **Location**: `src/main/resources/db/migrations/`
- **Naming**: `V{version}__{description}.sql` (e.g., `V1__initial.sql`)
- **Auto-execution**: Enabled on application startup

## Notes for Future Development

- **Logging**: Configured at package level with debug mode for `conectaseguros.co`
- **OpenAPI Documentation**: Available through Springdoc (Swagger UI)
- **Spring Data Redis**: Cache integration ready, keys and TTLs configurable per operation
- **Feign Clients**: Used for inter-service communication (clients-service, products-service)
- **Configuration Cache**: Gradle configuration cache enabled (`org.gradle.configuration-cache=true`)
- **Checkstyle**: Configuration located in `config/checkstyle/checkstyle.xml` - enforces Google style conventions
