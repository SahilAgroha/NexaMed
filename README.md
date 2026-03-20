# NexaMed — Medical Education Platform

Production-grade microservices platform for medical students. Built with Spring Boot, React, Kafka, Redis, and OpenAI.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4 · Java 21 · Spring Cloud 2025.1.0 |
| Service Discovery | Eureka Server |
| API Gateway | Spring Cloud Gateway + JWT filter |
| Inter-service calls | OpenFeign + Spring Cloud LoadBalancer |
| Frontend | React 18 · TypeScript · Tailwind CSS · Vite |
| Database | PostgreSQL (Supabase) — per-service schemas |
| Cache | Redis (remote VPS) |
| Messaging | Apache Kafka (remote VPS) |
| AI | OpenAI GPT-4o-mini |
| Real-time | WebSocket + STOMP |
| Video | WebRTC (signaling via WebSocket) |
| DevOps | Docker · Docker Compose · GitHub Actions |

## Services

| Service | Port | Responsibility |
|---|---|---|
| eureka-server | 8761 | Service registry |
| api-gateway | 8080 | Routing + JWT validation |
| auth-service | 8081 | Login · Register · OAuth2 · JWT |
| user-service | 8082 | User profiles |
| course-service | 8083 | Courses · Enrollment · Kafka events |
| ai-service | 8084 | Quiz gen · Case sim · Interview eval |
| interview-service | 8085 | Mock interviews · WebRTC signaling |
| notification-service | 8086 | Kafka consumer · WebSocket push |
| analytics-service | 8087 | Dashboard · Activity tracking |

## Quick Start

```bash
# 1. Clone
git clone https://github.com/yourusername/nexamed.git
cd nexamed

# 2. Configure environment
cp .env.example .env
# Fill in: Supabase URLs, Redis host, Kafka host, JWT secret, OpenAI key

# 3. Create Supabase schemas (run once)
# Open Supabase SQL Editor and run: supabase_schemas.sql

# 4. Start all services
docker-compose up -d

# 5. Open frontend
open http://localhost:3000
```

## Development (without Docker)

Start services in this order:
```bash
cd backend/eureka-server    && mvn spring-boot:run
cd backend/api-gateway       && mvn spring-boot:run
cd backend/auth-service      && mvn spring-boot:run
cd backend/user-service      && mvn spring-boot:run
cd backend/course-service    && mvn spring-boot:run
cd backend/ai-service        && mvn spring-boot:run
cd backend/interview-service && mvn spring-boot:run
cd backend/notification-service && mvn spring-boot:run
cd backend/analytics-service && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

## GitHub Secrets Required for CI/CD

| Secret | Description |
|---|---|
| DOCKERHUB_USERNAME | Docker Hub username |
| DOCKERHUB_TOKEN | Docker Hub access token |
| VPS_HOST | Production server IP |
| VPS_USER | SSH username |
| VPS_SSH_KEY | SSH private key |

## Architecture

```
Browser → API Gateway (8080)
           ├── /api/auth/**    → Auth Service (8081)
           ├── /api/users/**   → User Service (8082)
           ├── /api/courses/** → Course Service (8083)
           ├── /api/ai/**      → AI Service (8084)
           ├── /api/interviews/** → Interview Service (8085)
           ├── /api/notifications/** → Notification Service (8086)
           └── /api/analytics/** → Analytics Service (8087)

Kafka Topics:
  enrollment.created  → notification-service, analytics-service
  interview.completed → notification-service, analytics-service
  quiz.submitted      → notification-service, analytics-service
```