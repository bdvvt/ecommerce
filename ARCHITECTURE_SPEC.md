# KIẾN TRÚC HỆ THỐNG VÀ QUY CHUẨN KỸ THUẬT (SYSTEM SPECIFICATION)
## DỰ ÁN: OMNI E-COMMERCE MICROSERVICES

---

## 1. NGUYÊN TẮC GIAO TIẾP GIỮA CÁC MICROSERVICES (CORE RULES)

> [!IMPORTANT]
> **Quy tắc vàng phân định giữa Client và Event:**
> 1. **OpenFeign Client (Giao tiếp Đồng bộ - Synchronous):**
>    - Dùng **DUY NHẤT** khi cần **LẤY DỮ LIỆU TỨC THÌ (Query/Fetch Data)** từ service khác để phục vụ logic hiện tại.
>    - Các hàm Feign trả về dữ liệu (Data DTO) và chờ phản hồi ngay lập tức (Request - Response).
>    - Không dùng Feign để kích hoạt các hành động phụ (side-effects) như gửi email, gửi thông báo push.
> 2. **Kafka Events (Giao tiếp Bất đồng bộ - Asynchronous Event-Driven):**
>    - Dùng **DUY NHẤT** khi cần **XỬ LÝ SỰ KIỆN (Events / Side-effects)** diễn ra sau một hành động.
>    - Mô hình Fire-and-Forget: Service bắn Event lên Kafka Topic và kết thúc luồng ngay, không bắt người dùng phải đợi.
>    - Phục vụ: Gửi email xác nhận/OTP (`notification-service`), cập nhật tồn kho bất đồng bộ, ghi audit log, phân tích dữ liệu.

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MICROSERVICE INTERACTION                        │
├────────────────────────────────────────────────────────────────────────┤
│                                                                        │
│   [Service A] ──────( Feign Client: GET /data )──────> [Service B]    │
│       │               (Đồng bộ: Lấy dữ liệu ngay)                     │
│       │                                                                │
│       ▼                                                                │
│   [Kafka Broker] <────( Event: user.registered )────── [Identity Svc]  │
│       │               (Bất đồng bộ: Fire-and-forget)                   │
│       ▼                                                                │
│   [Notification Svc] ───> Gửi email chào mừng / OTP                    │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. HỆ THỐNG PHÂN QUYỀN (DYNAMIC RBAC - PERMISSION SYSTEM)

Hệ thống phân quyền được thiết kế chuẩn theo mô hình kiến trúc của **pm-xuatban-backend**:
- **Cấu trúc quyền định danh dạng `<feature>.<action>`**:
  - Module Users: `users.view`, `users.add`, `users.edit`, `users.delete`
  - Module Permissions: `permissions.view`, `permissions.edit`, `permissions.add`, `permissions.delete`
  - Module Products: `products.view`, `products.add`, `products.edit`, `products.delete`
  - Module Orders: `orders.view`, `orders.edit`, `orders.cancel`
- **Mô hình Database**:
  - `User` (N - N) `Role` (`users_roles`)
  - `Role` (N - N) `Permission` (`roles_permissions`)
  - Bảng `permissions` có trường `deleted_at` phục vụ Soft Delete.
- **Nạp quyền vào Spring Security**:
  - `MyUserDetailsService` nạp cả **Role** (`ROLE_ADMIN`, `ROLE_USER`) và **từng Permission** (`users.view`, `users.add`) vào danh sách `GrantedAuthority`.
  - Giúp bảo vệ Controller linh hoạt bằng cả `@PreAuthorize("hasAuthority('users.view')")` hoặc `@PreAuthorize("hasRole('ROLE_ADMIN')")`.
  - Có Security Bean `@Service("permissionService")` hỗ trợ SpEL: `@PreAuthorize("@permissionService.hasPermission('users.view')")`.
- **API Phân quyền phục vụ Frontend**:
  - `GET /api/v1/permissions/by-feature?name={featureName}`: Lấy danh sách quyền của tài khoản hiện tại đối với module cụ thể (Frontend dùng để ẩn/hiện nút "Thêm", "Sửa", "Xóa", menu chức năng).
- **API Quản lý Vai trò (Role Management - chuẩn pm-xuatban)**:
  - `GET /api/v1/roles`: Lấy toàn bộ danh sách vai trò đang hoạt động (dùng `findAll()` lọc `deletedAt IS NULL`).
  - `GET /api/v1/roles/{id}`: Chi tiết một vai trò kèm danh sách Permission.
  - `POST /api/v1/roles`: Tạo vai trò mới.
  - `PUT /api/v1/roles/{id}`: Cập nhật thông tin vai trò.
  - `DELETE /api/v1/roles/{id}`: Xóa mềm vai trò (`deleted_at`).
  - `GET /api/v1/roles/{roleId}/permissions`: Lấy danh sách chi tiết các quyền của vai trò.
  - `PUT /api/v1/roles/{roleId}/permissions`: Gán danh sách quyền cho vai trò.
- **Quản lý dữ liệu Quyền (Data Management)**:
  - Danh mục quyền được quản lý cố định gắn liền với mã nguồn hệ thống (`@PreAuthorize("hasAuthority('users.view')")`).
  - Dữ liệu quyền và vai trò được nạp trực tiếp qua các script SQL migration (không dùng `DataInitializer` trong Java code để tránh sinh dữ liệu giả lập).

---

## 3. NOTIFICATION SERVICE (PORT 8087)

Dịch vụ thông báo và gửi email bất đồng bộ:
- **Kafka Consumer**:
  - Lắng nghe sự kiện `user-registered` / `user-registered-topic` do `identity-service` phát ra khi có người dùng đăng ký tài khoản mới.
  - Tự động kích hoạt luồng gửi email chào mừng / kích hoạt tài khoản qua Gmail SMTP.
  - Sẵn sàng lắng nghe các sự kiện đơn hàng (`order-created`, `order-confirmed`, `order-status-changed`, `order-cancelled`, `payment-succeeded`).
- **REST Endpoints & OpenFeign Inter-Communication**:
  - `GET /api/v1/notifications/user/{userId}`: Cung cấp lịch sử thông báo của một người dùng cho `identity-service` (qua `NotificationClient`).
  - `GET /api/v1/notifications`: Lấy danh sách lịch sử thông báo có phân trang.
  - `GET /api/v1/notifications/{id}`: Xem chi tiết một thông báo.
  - `POST /api/v1/notifications/send`: Gửi thông báo trực tiếp qua API.
- **Tính chịu lỗi (Fault Tolerance)**:
  - Tự động kiểm tra trùng lặp thông báo (Idempotency) qua Redis khi Redis khả dụng. Nếu Redis chưa bật, luồng gửi thông báo vẫn tiếp tục hoạt động mà không bị gián đoạn.
  - Lưu trữ nhật ký thông báo vào MongoDB. Nếu database tạm thời gián đoạn, email vẫn được gửi thành công đến người nhận.

---

## 4. CỔNG ĐIỀU HƯỚNG GATEWAY (NGINX PORT 8000)

Cấu hình reverse proxy tập trung định tuyến toàn bộ request từ Client tới các microservice nội bộ:

| Prefix URL | Service đích | Cổng nội bộ | Mô tả |
| :--- | :--- | :--- | :--- |
| `/api/v1/auth/**` | `identity-service` | `8081` | Đăng nhập, đăng ký, refresh token |
| `/api/v1/users/**` | `identity-service` | `8081` | Quản lý người dùng, profile |
| `/api/v1/roles/**` | `identity-service` | `8081` | Quản lý vai trò (Role CRUD, gán quyền) |
| `/api/v1/admin/roles/**` | `identity-service` | `8081` | Quản lý vai trò nâng cao |
| `/api/v1/permissions/**` | `identity-service` | `8081` | Phân quyền, kiểm tra quyền theo module |
| `/api/v1/admin/permissions/**` | `identity-service` | `8081` | Quản trị quyền nâng cao |
| `/api/v1/notifications/**` | `notification-service` | `8087` | Quản lý thông báo, email |
| `/storage/**` | `minio` | `9000` | Lưu trữ và tải file ảnh/media tĩnh |
| `/swagger-ui/**`, `/v3/api-docs/**` | `identity-service` | `8081` | Tài liệu OpenAPI / Swagger UI |

---

## 4. BIẾN MÔI TRƯỜNG TẬP TRUNG (.env)

Tất cả các thông số kết nối (Database, JWT, SMTP, MinIO, Kafka, Service Ports) được quản lý tập trung tại file `.env` ở thư mục gốc của dự án:
- `GATEWAY_PORT=8000`
- `SERVER_PORT=8081`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/identity_db`
- `JWT_SECRET_KEY=...`
- `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- `MINIO_URL=http://localhost:9000`
- `NOTIFICATION_SERVICE_URL=http://localhost:8087`
