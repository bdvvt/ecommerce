package com.example.identityservice.models.services.uploads;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {
    private final Cloudinary cloudinary;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String cleanPublicId = "upload_" + System.currentTimeMillis();
            if (originalFilename != null && !originalFilename.isBlank()) {
                if (originalFilename.contains(".")) {
                    originalFilename = originalFilename.substring(0, originalFilename.lastIndexOf("."));
                }
                cleanPublicId = originalFilename.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + System.currentTimeMillis();
            }

            Map<?, ?> uploadParams = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "public_id", cleanPublicId
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl != null) {
                return secureUrl.toString();
            }
            Object url = uploadResult.get("url");
            return url != null ? url.toString() : null;
        } catch (Exception e) {
            log.error("Lỗi upload ảnh lên Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi tải ảnh lên Cloudinary: " + e.getMessage(), e);
        }
    }
}
