package com.ecommerce.orderhistory.util;

import java.time.LocalDateTime;

public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isFutureDate(LocalDateTime date) {
        if (date == null) return false;
        return date.isAfter(LocalDateTime.now());
    }

    public static boolean isPastOrPresent(LocalDateTime date) {
        if (date == null) return false;
        return !date.isAfter(LocalDateTime.now());
    }
}