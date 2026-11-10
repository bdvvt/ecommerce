# MICROSERVICES SPECIFICATION (TÓM TẮT QUY TẮC NHANH)

Xem tài liệu đầy đủ và chi tiết tại: [ARCHITECTURE_SPEC.md](file:///c:/Users/ADMIN88/IdeaProjects/Ecommerce/ARCHITECTURE_SPEC.md)

---

## 📌 2 NGUYÊN TẮC VÀNG BẮT BUỘC GHI NHỚ:

### 1. CLIENT (`OpenFeign` / REST Client) = ĐỒNG BỘ ĐỂ LẤY DỮ LIỆU
* **Mục đích:** Chỉ dùng khi cần **lấy dữ liệu (Query / Fetch Data)** từ service khác khi Service hiện tại cần dữ liệu ngay để hoàn thành luồng (Ví dụ: Order cần lấy thông tin User; Identity cần lấy lịch sử thông báo của User).
* **Đặc điểm:** Đồng bộ (Request - Response), phương thức `GET`, có Timeout & Circuit Breaker.
* **Quy ước:** Đặt trong package `client/`, interface đặt tên `<TargetService>Client.java`.

### 2. EVENT (`Apache Kafka`) = BẤT ĐỒNG BỘ ĐỂ XỬ LÝ SỰ KIỆN & THÔNG BÁO
* **Mục đích:** Dùng khi phát sinh sự kiện cần xử lý nền hoặc **gửi thông báo (Notification / Email / SMS)**.
* **Đặc điểm:** Bất đồng bộ (Pub/Sub, Fire-and-Forget), không làm nghẽn luồng người dùng (VD: User đăng ký xong thì lưu DB rồi bắn Event lên Kafka `user-registered-topic`, `notification-service` sẽ tự lắng nghe và gửi Email).
* **Quy ước:** Đặt trong package `models/events/`, tên class `<Entity><Action>Event.java`.
