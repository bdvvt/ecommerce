# HỆ THỐNG KIẾN TRÚC OMNI E-COMMERCE (MICROSERVICES SPECIFICATION)

> **Tài liệu đặc tả kiến trúc (Architecture Spec)**
> Dự án: **Omni E-Commerce Backend**
> Workspace: `C:\Users\ADMIN88\IdeaProjects\Ecommerce`

---

## 1. NGUYÊN TẮC CỐT LÕI (CORE ARCHITECTURAL RULES)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. CLIENT (OpenFeign / HTTP):  DÙNG ĐỂ LẤY DỮ LIỆU TỪ SERVICE KHÁC         │
│    (Synchronous Request-Response / Data Queries)                           │
│                                                                             │
│ 2. EVENT (Apache Kafka):       DÙNG ĐỂ XỬ LÝ SỰ KIỆN & GỬI THÔNG BÁO        │
│    (Asynchronous Event-Driven / Pub-Sub / Notification Handlers)           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.1. Khi nào dùng CLIENT (`OpenFeign`)?
* **Mục đích:** Giao tiếp **đồng bộ (Synchronous)** khi một service cần **lấy dữ liệu ngay lập tức** từ service khác để tiếp tục hoàn thành logic nghiệp vụ.
* **Đặc điểm:**
  * Có yêu cầu và chờ phản hồi (Request $\rightarrow$ Response).
  * Luôn sử dụng phương thức `GET` (hoặc các query filter phức tạp).
  * **KHÔNG** dùng Feign Client để gọi gửi email, SMS, notification hay các tác vụ ghi chậm gây nghẽn luồng người dùng.
* **Ví dụ thực tế:**
  * `order-service` gọi `UserClient.getUserById(userId)` để lấy tên, số điện thoại, địa chỉ nhận hàng của khách.
  * `identity-service` gọi `NotificationClient.getNotificationHistoryByUserId(userId)` để truy vấn lịch sử thông báo đã gửi cho user.
  * `order-service` gọi `ProductClient.checkStock(productId)` để xác thực số lượng tồn kho.

### 1.2. Khi nào dùng EVENT (`Apache Kafka`)?
* **Mục đích:** Giao tiếp **bất đồng bộ (Asynchronous)** khi một service vừa phát sinh một **sự kiện thay đổi trạng thái (State Change)** hoặc cần kích hoạt các tác vụ nền như **gửi thông báo (Email, SMS, Push Notification)**, tính điểm thưởng, audit log.
* **Đặc điểm:**
  * Bắn sự kiện rồi tiếp tục luồng ngay (Fire-and-forget / Non-blocking).
  * Tách rời hoàn toàn (Decoupled): Service gửi không cần quan tâm service nhận đang bận hay bảo trì; message được lưu trữ an toàn trong Kafka.
  * Không làm tăng thời gian phản hồi (latency) của API người dùng.
* **Ví dụ thực tế:**
  * Người dùng đăng ký tài khoản $\rightarrow$ `identity-service` lưu DB xong $\rightarrow$ Bắn `UserRegisteredEvent` vào Kafka topic `user-registered-topic` $\rightarrow$ Trả về kết quả đăng ký thành công cho Client ngay lập tức.
  * `notification-service` lắng nghe topic `user-registered-topic` $\rightarrow$ Tự động gửi email chào mừng / mã kích hoạt OTP tới hòm thư của user.

---

## 2. BẢN ĐỒ SERVICE VÀ PORT MAPPING

| Service Name | Port | Database / Storage | Vai trò chính |
| :--- | :---: | :--- | :--- |
| **API Gateway (Nginx)** | `8000` | - | Điểm vào duy nhất (Single Entrypoint), Routing, CORS, Rate Limit |
| **identity-service** | `8081` | PostgreSQL (`identity_db`) | Xác thực JWT, Quản lý tài khoản, Role/Permission, Địa chỉ |
| **notification-service** | `8087` | MongoDB / PostgreSQL | Consumer Kafka để gửi Email (SMTP), SMS, Push Notification |
| **product-service** *(dự kiến)* | `8082` | PostgreSQL (`product_db`) | Quản lý danh mục, sản phẩm, kho hàng, biến thể |
| **order-service** *(dự kiến)* | `8083` | PostgreSQL (`order_db`) | Giỏ hàng, tạo đơn hàng, thanh toán, xử lý đơn |
| **Apache Kafka** | `9092` | Zookeeper / KRaft | Message broker truyền tải Event giữa các service |
| **MinIO Storage** *(dự kiến)*| `9000` | Object Storage | Lưu trữ ảnh sản phẩm, banner, file hóa đơn |

---

## 3. QUY ƯỚC ĐẶT TÊN & TỔ CHỨC CODE (CODE CONVENTIONS)

### 3.1. Đối với Client (OpenFeign)
* **Vị trí package:** `com.example.<service>.client`
* **Quy ước đặt tên Interface:** `<TargetService>Client.java`
* **Quy ước DTO:** `com.example.<service>.client.dto.<TargetService>ResponseDto.java`
* **Mẫu chuẩn:**
```java
@FeignClient(name = "notification-service", url = "${notification.service.url:http://localhost:8087}")
public interface NotificationClient {

    // CHỈ DÙNG ĐỂ TRUY VẤN / LẤY DỮ LIỆU
    @GetMapping("/api/v1/notifications/user/{userId}")
    List<NotificationHistoryDto> getNotificationHistoryByUserId(@PathVariable("userId") Long userId);
}
```

### 3.2. Đối với Event (Kafka)
* **Vị trí Event Model:** `com.example.<service>.models.events`
* **Quy ước đặt tên Event Class:** `<Entity><Action>Event.java` (Ví dụ: `UserRegisteredEvent`, `OrderCreatedEvent`, `PaymentCompletedEvent`)
* **Vị trí Publisher:** `com.example.<service>.models.services.impl.<Entity>EventPublisher.java`
* **Mẫu Event chuẩn:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {
    private String eventId;           // UUID duy nhất của event
    private String eventType;         // "USER_REGISTERED"
    private Long userId;              // ID người dùng
    private String email;             // Email nhận thông báo
    private String fullName;
    private String loginIdentifier;
    private LocalDateTime timestamp;
}
```

### 3.3. Đối với Kafka Topics
* Topic liên quan đến User/Auth: `user-registered-topic`, `user-password-reset-topic`
* Topic liên quan đến Order: `order-created-topic`, `order-status-changed-topic`
* Topic liên quan đến Notification: `notification-events-topic`

---

## 4. API GATEWAY ROUTING SPECIFICATION (`nginx.conf`)

Tất cả request từ Frontend / Mobile App đều đi qua Nginx Gateway tại port `8000`:

```
                    ┌─────────────────────────┐
                    │ Nginx Gateway (:8000)   │
                    └────────────┬────────────┘
                                 │
     ┌───────────────────────────┼───────────────────────────┐
     │                           │                           │
     ▼                           ▼                           ▼
/api/v1/auth/**             /api/v1/notifications/**     /storage/**
/api/v1/users/**            → notification-service:8087  → minio:9000
→ identity-service:8081
```

* Gateway tự động kiểm soát CORS cho localhost (React, Vue, Flutter, Postman).
* Rate limiting: 50 requests/giây (`burst=25 nodelay`).
* Upload body size: Tối đa 50MB.

---

## 5. BẢNG DANH MỤC BIẾN MÔI TRƯỜNG (`.env`)

Mọi thông số nhạy cảm được cấu hình tập trung tại file `.env` ở thư mục gốc:

| Biến | Ý nghĩa | Mặc định |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port của identity-service | `8081` |
| `GATEWAY_PORT` | Port của Nginx Gateway | `8000` |
| `SPRING_DATASOURCE_URL` | JDBC URL kết nối PostgreSQL | `jdbc:postgresql://localhost:5432/identity_db` |
| `SPRING_DATASOURCE_USERNAME` | Username DB | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Password DB | `12345` |
| `JWT_SECRET_KEY` | Khóa bí mật ký token | Chuỗi mã hóa bí mật $\ge$ 256 bits |
| `JWT_EXPIRED` | Thời gian sống access token (ms) | `3600000` (1 giờ) |
| `SPRING_MAIL_USERNAME` | Tài khoản Gmail gửi mail | `bdvviet@gmail.com` |
| `SPRING_MAIL_PASSWORD` | App Password Gmail | `wnoc jtud djgz dwlu` |
| `KAFKA_BOOTSTRAP_SERVERS` | Địa chỉ Kafka Broker | `localhost:9092` |
| `KAFKA_GROUP_ID` | Consumer group id | `ecommerce-identity-group` |
| `NOTIFICATION_SERVICE_URL` | URL gọi trực tiếp notification | `http://localhost:8087` |

---

## 6. CHECKLIST KHI TẠO THÊM SERVICE MỚI
1. [ ] Khai báo Database riêng (không share DB giữa các service).
2. [ ] Thêm upstream và location route tương ứng vào `microservice-gateway/nginx/nginx.conf`.
3. [ ] Nếu cần **lấy dữ liệu đồng bộ** từ service khác $\rightarrow$ Tạo Feign `@Client` trong package `client/`.
4. [ ] Nếu cần **thông báo hoặc kích hoạt tác vụ** $\rightarrow$ Publish Kafka Event lên Topic, không gọi HTTP đè lên luồng chính.
5. [ ] Đăng ký biến cấu hình vào file `.env` và `.env.example`.
