package com.example.identityservice.validations.annotations;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.identityservice.validations.handle.FileExtensionValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileExtensionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileExtension {
    String message() default "Invalid file extension";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] allowedExtensions() default {".jpg",".png",".jpeg",".webp",".mp4"};
}
