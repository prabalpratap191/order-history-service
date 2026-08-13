package com.ecommerce.orderhistory.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that an order date is not in the future.
 * Legacy Business Rule: Order date cannot be future date (legacy_app_spec.md Section 5.2)
 */
@Documented
@Constraint(validatedBy = OrderDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrderDate {
    String message() default "Order date cannot be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}