# Library API

JWT 기반 인증과 도서 대출·예약 관리 기능을 제공하는 RESTful API 서버입니다.
Spring Boot와 Oracle DB를 기반으로 구축하였으며, Docker Compose로 손쉽게 실행할 수 있습니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Backend** | Java 17, Spring Boot 4.1.0, Spring Data JPA |
| **Security** | Spring Security 6, JWT (jjwt) |
| **Database** | Oracle XE 21c |
| **Infra** | Docker, Docker Compose |
| **Docs** | Swagger UI (springdoc-openapi) |

---

## 주요 기능

- **회원 인증** — 회원가입 / 로그인 (JWT 발급)
- **도서 관리** — 도서 목록·상세 조회, 제목/저자 검색, 페이징 (관리자: 등록·수정·삭제)
- **대출 관리** — 대출 신청 / 반납, 내 대출 이력 조회
- **예약 관리** — 예약 신청 / 취소, 내 예약 목록 조회
- **관리자 기능** — 전체 회원·대출·예약 현황 조회, 연체 도서 관리

---

## 프로젝트 구조

```
src/main/java/com/library/libraryapi/
├── config/          # SecurityConfig, SwaggerConfig, AppConfig
├── controller/      # Auth, Book, Loan, Reservation, Admin
├── domain/
│   ├── entity/      # Member, Book, Loan, Reservation
│   └── enums/       # MemberRole, BookStatus, LoanStatus, ReservationStatus
├── dto/
│   ├── request/     # LoginRequest, RegisterRequest, BookRequest
│   └── response/    # LoginResponse, BookResponse, LoanResponse, ...
├── exception/       # CustomException, GlobalExceptionHandler, ErrorResponse
├── repository/      # Spring Data JPA Repositories
├── security/        # JwtTokenProvider, JwtAuthenticationFilter, UserDetailsServiceImpl
└── service/         # AuthService, BookService, LoanService, ReservationService, AdminService
```

---

## API 엔드포인트

### 인증 (`/api/auth`)

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| `POST` | `/api/auth/register` | 회원가입 | 없음 |
| `POST` | `/api/auth/login` | 로그인 (JWT 발급) | 없음 |

### 도서 (`/api/books`)

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| `GET` | `/api/books` | 도서 목록 조회 (페이징·검색) | USER |
| `GET` | `/api/books/{id}` | 도서 상세 조회 | USER |
| `POST` | `/api/books` | 도서 등록 | ADMIN |
| `PUT` | `/api/books/{id}` | 도서 수정 | ADMIN |
| `DELETE` | `/api/books/{id}` | 도서 삭제 | ADMIN |

### 대출 (`/api/loans`)

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| `GET` | `/api/loans` | 내 대출 목록 조회 | USER |
| `POST` | `/api/loans/{bookId}` | 대출 신청 | USER |
| `PUT` | `/api/loans/return/{id}` | 반납 | USER |

### 예약 (`/api/reservations`)

| 메서드 | 엔드포인트 | 설명 | 권한 |
|--------|-----------|------|------|
| `GET` | `/api/reservations` | 내 예약 목록 조회 | USER |
| `POST` | `/api/reservations/{bookId}` | 예약 신청 | USER |
| `DELETE` | `/api/reservations/{id}` | 예약 취소 | USER |

### 관리자 (`/api/admin`) — ADMIN 전용

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| `GET` | `/api/admin/members` | 전체 회원 목록 (페이징) |
| `GET` | `/api/admin/members/{id}` | 회원 상세 (대출 이력 포함) |
| `GET` | `/api/admin/loans` | 전체 대출 현황 (status 필터) |
| `GET` | `/api/admin/loans/overdue` | 연체 목록 (자동 상태 업데이트) |
| `GET` | `/api/admin/reservations` | 전체 예약 현황 (페이징) |

---

## 주요 구현 포인트

### 1. JWT 인증 방식

로그인 성공 시 `username`과 `role` 클레임을 담은 JWT를 발급합니다 (HMAC-SHA256, 유효 24시간).  
이후 요청은 `Authorization: Bearer <token>` 헤더를 통해 인증하며, `JwtAuthenticationFilter`가 매 요청마다 토큰을 검증하고 `SecurityContext`에 사용자 정보를 주입합니다.

### 2. `@Transactional` + 비관적 락을 통한 동시성 처리

대출 신청 시 `SELECT ... FOR UPDATE` (비관적 락)로 도서 레코드를 조회하여 동시 대출 요청으로 인한 데이터 충돌을 방지합니다.

```java
// BookRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM Book b WHERE b.id = :id")
Optional<Book> findByIdWithLock(Long id);
```

### 3. 도서 상태 자동 변경 로직

반납 처리 시 해당 도서에 대기 중인 예약이 있으면 첫 번째 예약자의 상태를 `COMPLETED`로 전환하고 도서 상태를 `RESERVED`로 변경합니다. 예약이 없으면 `AVAILABLE`로 복구됩니다.

```
대출 중 (LOANED)
    └─ 반납 처리
         ├─ 대기 예약 존재 → 도서 RESERVED, 예약 COMPLETED (예약자 대출 가능)
         └─ 대기 예약 없음  → 도서 AVAILABLE
```

### 4. `ROLE_ADMIN` 권한 처리

Spring Security의 `@PreAuthorize("hasRole('ADMIN')")` 어노테이션으로 관리자 엔드포인트를 보호합니다. `AdminController` 클래스 레벨에 선언하여 모든 관리자 API에 일괄 적용합니다.

### 5. 연체 자동 감지 (`@Scheduled`)

매일 자정에 스케줄러가 실행되어 반납 기한이 지난 `ACTIVE` 상태의 대출을 `OVERDUE`로 일괄 변경합니다.

```java
@Scheduled(cron = "0 0 0 * * *")
@Transactional
public void updateOverdueLoans() { ... }
```

---

## 실행 방법

### 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

```env
DB_URL=jdbc:oracle:thin:@oracle:1521/XEPDB1
DB_USERNAME=system
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key_at_least_32_characters
```

### Docker Compose로 실행

```bash
docker-compose up --build
```

Oracle DB가 정상 기동된 후 API 서버가 자동으로 연결됩니다 (`healthcheck` 설정으로 순서 보장).

### Swagger UI 접속

```
http://localhost:8080/swagger-ui/index.html
```

로그인 후 발급받은 JWT를 우측 상단 **Authorize** 버튼에 입력하면 인증이 필요한 API를 바로 테스트할 수 있습니다.

---

## ERD

> 이미지 추가 예정

---

## 도서 상태 흐름

```
AVAILABLE ──대출──▶ LOANED ──반납(예약 없음)──▶ AVAILABLE
                        └──반납(예약 있음)──▶ RESERVED ──예약자 대출──▶ LOANED
```
