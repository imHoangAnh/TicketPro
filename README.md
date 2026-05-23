# Event Ticket Booking System

He thong dat ve su kien gom backend Java / Spring Boot theo huong DDD
multi-module va frontend React / Vite. Du an ho tro luong chinh: xem su kien,
chon hang ve, dat ve, giu ton kho bang Redis, thanh toan VNPAY, va trang quan tri cho admin.

## Tinh Nang Chinh

- Dang ky, dang nhap bang email/password.
- JWT access token va refresh token opaque luu trong Redis.
- Phan quyen `USER` va `ADMIN`.
- Public user xem danh sach su kien dang active va chi tiet hang ve.
- User dat ve, xem don hang cua minh, huy don hang dang `PENDING`.
- Redis Lua lam cong dat ve atomic truoc khi cap nhat ton kho MySQL.
- Thanh toan mock success va VNPAY sandbox.
- Admin tao/sua/an/hien/xoa mem su kien va hang ve.
- Admin xem don hang.
- Frontend React co cac man hinh public, auth, cart/payment va dashboard admin.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3.5, Maven |
| Frontend | React, Vite, Axios, React Router, Lucide icons |
| Database | MySQL 8 |
| Cache/Session/Stock | Redis |
| Security | Spring Security, JWT, BCrypt |
| Payment | Mock payment, VNPAY sandbox |
| Local runtime | Docker Compose |

## Kien Truc Module

Backend duoc tach module theo huong DDD-flavoured clean architecture:

```text
xxxx-start
  -> xxxx-controller
  -> xxxx-infrastructure

xxxx-controller
  -> xxxx-application

xxxx-application
  -> xxxx-domain

xxxx-infrastructure
  -> xxxx-domain
  -> xxxx-application contracts when needed
```

Vai tro tung module:

- `xxxx-start`: Spring Boot entrypoint, runtime composition root, config.
- `xxxx-controller`: REST controllers, security filter, request/response DTO.
- `xxxx-application`: use case orchestration, transaction boundary, auth,
  order, payment, cache/port contracts.
- `xxxx-domain`: entity, enum, domain service, repository contract, business
  rules.
- `xxxx-infrastructure`: JPA adapter, Redis adapter, Redisson lock, VNPAY
  gateway adapter.
- `xxxx.fe.com`: React frontend.

Runtime flow:

```text
React frontend
  -> REST controller
      -> application use case
          -> domain service / repository contract
              -> infrastructure adapter
                  -> MySQL, Redis, VNPAY
```

## Thu Muc Quan Trong

```text
.
|-- xxxx-start/             # Spring Boot main app and application.yml
|-- xxxx-controller/        # REST API, security, web config
|-- xxxx-application/       # Use cases, DTOs, ports
|-- xxxx-domain/            # Domain model and contracts
|-- xxxx-infrastructure/    # MySQL/Redis/VNPAY adapters
|-- xxxx.fe.com/            # React/Vite frontend
|-- environment/            # Docker Compose and MySQL init SQL
`-- docs/                   # Architecture, product contracts, story packets
```

## Yeu Cau Moi Truong

Can cai san:

- Docker Desktop.
- Java 21.
- Maven.
- Node.js 20+ hoac 22 LTS.
- npm.

Kiem tra:

```powershell
docker --version
docker compose version
java -version
mvn -version
node -v
npm -v
```

Neu dung JDK co san tren may hien tai:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

## Chay MySQL Va Redis Bang Docker

Tu thu muc root:

```powershell
docker-compose -f environment\docker-compose-dev.yml up -d
```

Thong tin ket noi local:

| Service | Host | Port | Username | Password | Database |
| --- | --- | --- | --- | --- | --- |
| MySQL | `localhost` | `3316` | `root` | `root1234` | `vetautet` |
| Redis | `127.0.0.1` | `6319` | none | none | n/a |

Kiem tra container:

```powershell
docker ps
docker logs pre-event-mysql
docker logs pre-event-redis
```

File init SQL nam o:

```text
environment/mysql/init/ticket_init.sql
```

Luu y: MySQL chi chay init SQL khi data directory con trong. Neu can reset DB
local:

```powershell
docker-compose -f environment\docker-compose-dev.yml down
Rename-Item environment\data\db_data environment\data\db_data_backup
docker-compose -f environment\docker-compose-dev.yml up -d
```

Lenh tren se doi ten data folder cu. Du lieu local cu se khong con duoc dung.

## Ket Noi MySQL Bang DataGrip

Tao data source MySQL trong DataGrip:

```text
Host: localhost
Port: 3316
User: root
Password: root1234
Database: vetautet
```

JDBC URL:

```text
jdbc:mysql://localhost:3316/vetautet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
```

Sau khi connect, tick schema `vetautet` trong `Databases...`, roi mo
`vetautet -> tables`.

Bang seed chinh:

```text
users
roles
user_roles
events
ticket_types
orders
order_items
payments
```

## Chay Backend

Backend chay port `1122`.

Build jar:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

mvn -pl xxxx-start -am clean package
```

Chay jar:

```powershell
java -jar xxxx-start\target\xxxx-start-1.0-SNAPSHOT.jar
```

Backend chay thanh cong khi log co:

```text
Tomcat started on port 1122
Started StartApplication
```

Kiem tra API public:

```powershell
Invoke-RestMethod http://localhost:1122/api/events
```

Hoac mo tren browser:

```text
http://localhost:1122/api/events
```

Neu muon chay bang Spring Boot Maven Plugin, dung full coordinate:

```powershell
mvn -pl xxxx-start -am org.springframework.boot:spring-boot-maven-plugin:3.3.5:run
```

## Chay Frontend

Mo terminal khac:

```powershell
npm install
npm run dev
```

Frontend mac dinh chay tai:

```text
http://localhost:5173
```

Frontend dang goi backend truc tiep qua:

```text
http://localhost:1122
```

CORS backend dang allow:

```text
http://localhost:5173
```

Vi vay nen mo frontend bang `localhost`, khong nen dung `127.0.0.1`.

Build frontend:

```powershell
npm run build
```

## Tai Khoan Test

Neu DB duoc khoi tao tu `ticket_init.sql`, co san:

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@example.com` | `password123` |
| User | `user@example.com` | `password123` |

Flow user:

1. Dang nhap `user@example.com`.
2. Vao `/tickets`.
3. Chon su kien.
4. Chon hang ve va so luong.
5. Bam dat ve de vao `/cart`.
6. Bam tiep tuc dat ve va thanh toan.
7. Chon mock payment hoac VNPAY sandbox.

Luu y: vao thang `/cart` se khong co route state nen trang se bao khong co ve
trong gio hang. Can di tu trang chi tiet su kien sang cart.

## API Chinh

Public:

```text
GET /api/events
GET /api/events/{eventId}
```

Auth:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
```

User:

```text
POST /api/orders
GET  /api/orders/my
GET  /api/orders/{orderId}
PUT  /api/orders/{orderId}/cancel
POST /api/payments/{orderId}/mock-success
POST /api/payments/{orderId}/vnpay
```

Admin:

```text
POST   /api/admin/events
PUT    /api/admin/events/{eventId}
DELETE /api/admin/events/{eventId}
PUT    /api/admin/events/{eventId}/active
PUT    /api/admin/events/{eventId}/inactive

POST   /api/admin/events/{eventId}/ticket-types
PUT    /api/admin/ticket-types/{ticketTypeId}
DELETE /api/admin/ticket-types/{ticketTypeId}

GET    /api/admin/orders
GET    /api/admin/orders/{orderId}
```

Tao order:

```json
{
  "ticketTypeId": 1,
  "quantity": 2
}
```

## Cau Hinh Quan Trong

Backend config:

```text
xxxx-start/src/main/resources/application.yml
```

Gia tri mac dinh:

```yaml
server:
  port: 1122

spring:
  datasource:
    url: jdbc:mysql://localhost:3316/vetautet
    username: root
    password: root1234
  data:
    redis:
      host: 127.0.0.1
      port: 6319
```

Bien moi truong co the override:

```text
AUTH_JWT_SECRET
AUTH_ACCESS_TOKEN_TTL_MINUTES
AUTH_REFRESH_TOKEN_TTL_DAYS
AUTH_REFRESH_COOKIE_SECURE
VNPAY_TMN_CODE
VNPAY_SECRET_KEY
VNPAY_PAY_URL
VNPAY_RETURN_URL
```

## Validation

Backend:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

mvn test
mvn package
```

Frontend:

```powershell
cd xxxx.fe.com
npm run build
```

Docker Compose config:

```powershell
docker-compose -f environment\docker-compose-dev.yml config
```

## Loi Thuong Gap

### Maven bao `No plugin found for prefix spring-boot`

Dung full plugin coordinate:

```powershell
mvn -pl xxxx-start -am org.springframework.boot:spring-boot-maven-plugin:3.3.5:run
```

Hoac build jar va chay:

```powershell
mvn -pl xxxx-start -am clean package
java -jar xxxx-start\target\xxxx-start-1.0-SNAPSHOT.jar
```

### Backend khong connect duoc MySQL

Kiem tra container va port:

```powershell
docker ps
docker logs pre-event-mysql
```

MySQL phai map:

```text
3316 -> 3306
```

### Backend khong connect duoc Redis

Kiem tra:

```powershell
docker ps
docker logs pre-event-redis
```

Redis phai map:

```text
6319 -> 6379
```


## Tai Lieu Du An

- `docs/ARCHITECTURE.md`: kien truc va module boundary.
- `docs/product/`: product contract hien tai.
- `docs/stories/`: story packets theo tung epic.
- `docs/TEST_MATRIX.md`: bang mapping behavior sang validation evidence.
- `docs/decisions/`: cac quyet dinh kien truc/san pham quan trong.