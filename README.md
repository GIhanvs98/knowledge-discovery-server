# AI-Powered Enterprise Knowledge Assistant - Eureka Discovery Server

## Overview
The **Eureka Discovery Server** (`discovery-server`) is the foundational service registry component for the **AI-Powered Enterprise Knowledge Assistant** microservices platform. Built using **Spring Boot 3.x** and **Spring Cloud Netflix Eureka Server**, this service enables dynamic service registration, client-side load balancing, and decoupled service discovery across all polyrepo microservices.

---

## Architectural Decisions & Principles

1. **Role in Microservices Architecture**:
   - Acts as the central telephone exchange for services (e.g., Knowledge Ingestion Service, Query Processing Service, Vector Store Service, Identity Service).
   - Microservices register their network locations dynamically upon startup, enabling resilience against instance scaling, IP changes, and container recreation.

2. **Standalone Configuration**:
   - Configured specifically in standalone mode (`register-with-eureka: false`, `fetch-registry: false`).
   - The Discovery Server does not register itself into its own registry or attempt peer-to-peer replication, keeping single-instance development and deployment minimal, predictable, and resource-efficient.
   - Self-preservation is disabled (`enable-self-preservation: false`) to evict terminated instances promptly during development and testing.

3. **Externalized Environment Configuration (`.env`)**:
   - Follows 12-factor application design: configurations are externalized through environment variables and local `.env` files.
   - A `.env.example` template is committed to version control, while `.env` is ignored via `.gitignore` to prevent credential or environment leakage.
   - The application automatically detects and loads `.env` variables on startup with fallback defaults.

4. **Production-Minded Observability**:
   - Integrates **Spring Boot Actuator** exposing dedicated health (`/actuator/health`) and info (`/actuator/info`) endpoints.
   - Provides liveness and readiness probe compatibility for container orchestrators (e.g., Kubernetes, Docker Swarm).

5. **Clean & Minimal Design**:
   - Follows clean architecture and SOLID principles: zero business logic, clean separation of configuration and bootstrap entrypoints, avoiding unnecessary abstractions.

---

## Technology Stack

- **Language / Runtime**: Java 21 (LTS)
- **Framework**: Spring Boot 3.3.5
- **Service Discovery**: Spring Cloud Netflix Eureka Server (Spring Cloud BOM 2023.0.3)
- **Observability**: Spring Boot Actuator
- **Build Tool**: Apache Maven

---

## Project Structure

```text
discovery-server/
├── .env                  # Local environment configurations (git-ignored)
├── .env.example          # Environment configuration template
├── .gitignore            # Git exclusion rules
├── pom.xml               # Maven POM with Spring Boot & Cloud dependencies
├── README.md             # Project documentation and architecture guide
└── src/
    ├── main/
    │   ├── java/
    │   │   └── lk/
    │   │       └── facadecenter/
    │   │           └── knowledge/
    │   │               └── discovery/
    │   │                   ├── DiscoveryServerApplication.java
    │   │                   └── config/
    │   │                       └── DiscoveryServerConfig.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
            └── lk/
                └── facadecenter/
                    └── knowledge/
                        └── discovery/
                            └── DiscoveryServerApplicationTests.java
```

---

## Environment Variables & `.env` Reference

Copy `.env.example` to create your local `.env`:
```bash
cp .env.example .env
```

| Variable | Description | Default Value |
|---|---|---|
| `SERVER_PORT` | HTTP port for the Eureka Server | `8761` |
| `SPRING_APPLICATION_NAME` | Service discovery application name | `discovery-server` |
| `EUREKA_HOSTNAME` | Hostname for Eureka service registry | `localhost` |
| `EUREKA_REGISTER_WITH_EUREKA` | Self-registration toggle | `false` |
| `EUREKA_FETCH_REGISTRY` | Peer registry fetch toggle | `false` |
| `EUREKA_SERVICE_URL` | Eureka defaultZone service URL | `http://localhost:8761/eureka/` |
| `EUREKA_ENABLE_SELF_PRESERVATION` | Eureka self-preservation mode | `false` |
| `EUREKA_EVICTION_INTERVAL_TIMER_IN_MS` | Eviction task timer interval (ms) | `10000` |
| `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE` | Actuator exposed endpoints | `health,info` |
| `MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS` | Actuator health detail level | `always` |

---

## Configuration Reference (`application.yml`)

```yaml
server:
  port: ${SERVER_PORT:8761}

spring:
  application:
    name: ${SPRING_APPLICATION_NAME:discovery-server}

eureka:
  instance:
    hostname: ${EUREKA_HOSTNAME:localhost}
  client:
    # Standalone mode: Eureka Discovery Server should not register itself as a client
    register-with-eureka: ${EUREKA_REGISTER_WITH_EUREKA:false}
    # Standalone mode: Server does not fetch registries from peer nodes
    fetch-registry: ${EUREKA_FETCH_REGISTRY:false}
    service-url:
      defaultZone: ${EUREKA_SERVICE_URL:http://${eureka.instance.hostname}:${server.port}/eureka/}
  server:
    # Disable self-preservation for standalone development / single node environment
    enable-self-preservation: ${EUREKA_ENABLE_SELF_PRESERVATION:false}
    eviction-interval-timer-in-ms: ${EUREKA_EVICTION_INTERVAL_TIMER_IN_MS:10000}

# Spring Boot Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        # Expose only health and info endpoints as required
        include: ${MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE:health,info}
  endpoint:
    health:
      # Show detailed health status for monitoring
      show-details: ${MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS:always}
      probes:
        enabled: true
  info:
    env:
      enabled: true

info:
  app:
    name: "@project.name@"
    description: "@project.description@"
    version: "@project.version@"
```

---

## Setup & Running Instructions

### Prerequisites
- **JDK 21** installed and configured (`JAVA_HOME` pointing to Java 21)
- **Apache Maven 3.8+** installed

### 1. Build the Service
From within the `discovery-server` directory:
```bash
cd /home/gihanvs/projects/final/discovery-server
mvn clean package
```

### 2. Run the Service
Run directly using the Spring Boot Maven plugin:
```bash
mvn spring-boot:run
```

Or run the packaged executable JAR:
```bash
java -jar target/discovery-server-0.0.1-SNAPSHOT.jar
```

### 3. Verify Startup and Health
Once the server starts on port `8761`:

- **Eureka Web Dashboard**:  
  Navigate to [http://localhost:8761](http://localhost:8761) to view the registered microservice instances.
- **Actuator Health Check**:  
  ```bash
  curl http://localhost:8761/actuator/health
  ```
  Expected response: `{"status":"UP",...}`
- **Actuator Info Endpoint**:  
  ```bash
  curl http://localhost:8761/actuator/info
  ```

---

## Client Microservices Integration Example
To register downstream microservices (e.g., Knowledge Ingestion, Query Engine) with this discovery server, configure their `application.yml` as follows:

```yaml
spring:
  application:
    name: knowledge-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```
