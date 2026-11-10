package org.example.notificationservice.dto.mapper;

import org.example.notificationservice.dto.event.*;
import org.example.notificationservice.dto.req.SendNotificationReq;
import org.example.notificationservice.dto.res.NotificationRes;
import org.example.notificationservice.entity.NotificationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", imports = {StringUtils.class, Instant.class, UUID.class})
public interface NotificationMapper {

    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "eventId", expression = "java(StringUtils.hasText(req.getEventId()) ? req.getEventId() : UUID.randomUUID().toString())")
    @Mapping(target = "channel", expression = "java(StringUtils.hasText(req.getChannel()) ? req.getChannel() : \"EMAIL\")")
    @Mapping(target = "status", constant = "QUEUED")
    @Mapping(target = "providerRef", expression = "java(\"SMTP-\" + UUID.randomUUID().toString().substring(0, 8))")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    NotificationLog toEntity(SendNotificationReq req);

    NotificationRes toResponse(NotificationLog notificationLog);

    List<NotificationRes> toResponseList(List<NotificationLog> notificationLogs);

    @Mapping(target = "eventId", expression = "java(\"ORDER_CREATED_\" + event.getOrderId())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", expression = "java(StringUtils.hasText(event.getCustomerRef()) ? event.getCustomerRef() : \"customer@example.com\")")
    @Mapping(target = "subject", expression = "java(\"Xác nhận đơn hàng #\" + event.getOrderId())")
    @Mapping(target = "content", expression = "java(\"Đơn hàng #\" + event.getOrderId() + \" trị giá \" + event.getAmount() + \" \" + event.getCurrency() + \" đã được tạo thành công. Vui lòng hoàn tất thanh toán trước \" + event.getReservationExpiry() + \".\")")
    SendNotificationReq toReq(OrderCreated event);

    @Mapping(target = "eventId", expression = "java(\"ORDER_CONFIRMED_\" + event.getOrderId())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", constant = "customer@example.com")
    @Mapping(target = "subject", expression = "java(\"Đơn hàng #\" + event.getOrderId() + \" đã được xác nhận\")")
    @Mapping(target = "content", expression = "java(\"Đơn hàng #\" + event.getOrderId() + \" đã được hệ thống xác nhận vào lúc \" + event.getConfirmedAt() + \" và đang được chuẩn bị giao hàng.\")")
    SendNotificationReq toReq(OrderConfirmed event);

    @Mapping(target = "eventId", expression = "java(\"ORDER_STATUS_\" + event.getOrderId() + \"_\" + event.getNewStatus())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", constant = "customer@example.com")
    @Mapping(target = "subject", expression = "java(\"Cập nhật trạng thái đơn hàng #\" + event.getOrderId())")
    @Mapping(target = "content", expression = "java(\"Đơn hàng #\" + event.getOrderId() + \" đã thay đổi trạng thái từ \" + event.getOldStatus() + \" sang \" + event.getNewStatus() + \".\")")
    SendNotificationReq toReq(OrderStatusChanged event);

    @Mapping(target = "eventId", expression = "java(\"ORDER_CANCELLED_\" + event.getOrderId())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", constant = "customer@example.com")
    @Mapping(target = "subject", expression = "java(\"Thông báo hủy đơn hàng #\" + event.getOrderId())")
    @Mapping(target = "content", expression = "java(\"Đơn hàng #\" + event.getOrderId() + \" đã bị hủy. Lý do: \" + event.getReason() + \".\" + (Boolean.TRUE.equals(event.getRefundRequired()) ? \" Khoản tiền sẽ được hoàn lại theo quy định.\" : \"\"))")
    SendNotificationReq toReq(OrderCancelled event);

    @Mapping(target = "eventId", expression = "java(\"PAYMENT_SUCCEEDED_\" + event.getPaymentId())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", constant = "customer@example.com")
    @Mapping(target = "subject", expression = "java(\"Xác nhận thanh toán thành công đơn hàng #\" + event.getOrderId())")
    @Mapping(target = "content", expression = "java(\"Thanh toán thành công số tiền \" + event.getAmount() + \" \" + event.getCurrency() + \" cho đơn hàng #\" + event.getOrderId() + \". Mã đối tác thanh toán: \" + event.getProviderRef() + \".\")")
    SendNotificationReq toReq(PaymentSucceeded event);

    @Mapping(target = "eventId", expression = "java(\"PAYMENT_FAILED_\" + event.getPaymentId())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", constant = "customer@example.com")
    @Mapping(target = "subject", expression = "java(\"Thông báo thanh toán thất bại đơn hàng #\" + event.getOrderId())")
    @Mapping(target = "content", expression = "java(\"Thanh toán cho đơn hàng #\" + event.getOrderId() + \" không thành công. Lý do: \" + event.getFailureCategory() + \" (\" + event.getFailureReason() + \"). Vui lòng thử lại.\")")
    SendNotificationReq toReq(PaymentFailed event);

    @Mapping(target = "eventId", expression = "java(\"REFUND_COMPLETED_\" + event.getRefundId())")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", constant = "customer@example.com")
    @Mapping(target = "subject", expression = "java(\"Xác nhận hoàn tiền cho đơn hàng #\" + event.getOrderId())")
    @Mapping(target = "content", expression = "java(\"Khoản tiền hoàn \" + event.getAmount() + \" cho đơn hàng #\" + event.getOrderId() + \" (Mã hoàn tiền: \" + event.getRefundId() + \") đã được hoàn tất thành công.\")")
    SendNotificationReq toReq(RefundCompleted event);

    @Mapping(target = "eventId", expression = "java(StringUtils.hasText(event.getEventId()) ? event.getEventId() : (\"USER_REGISTERED_\" + event.getUserId()))")
    @Mapping(target = "channel", constant = "EMAIL")
    @Mapping(target = "recipient", expression = "java(StringUtils.hasText(event.getEmail()) ? event.getEmail() : \"user@example.com\")")
    @Mapping(target = "subject", expression = "java(StringUtils.hasText(event.getOtp()) ? \"Mã OTP xác thực tài khoản Omni E-Commerce\" : \"Chào mừng bạn gia nhập hệ thống Omni E-Commerce\")")
    @Mapping(target = "content", expression = "java(StringUtils.hasText(event.getOtp()) ? (\"Chào \" + (StringUtils.hasText(event.getFullName()) ? event.getFullName() : (event.getLoginIdentifier() != null ? event.getLoginIdentifier() : \"\")) + \", mã OTP xác thực của bạn là: \" + event.getOtp() + \". Mã có hiệu lực trong 5 phút.\") : (\"Chào \" + event.getFullName() + \", tài khoản của bạn đã được đăng ký thành công trên hệ thống Omni E-Commerce.\"))")
    SendNotificationReq toReq(UserRegistered event);
}
