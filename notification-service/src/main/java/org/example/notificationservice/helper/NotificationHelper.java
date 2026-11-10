package org.example.notificationservice.helper;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationHelper {

    /**
     * FR-NOT-009: PII trong log thông báo PHẢI được che hoặc loại bỏ
     */
    public String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}
