package pizza_shop_system.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    // Formatter for ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ
    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // Method to get current time in the desired format
    public String getCurrentDateTime() {
        return FORMATTER.format(LocalDateTime.now(ZoneId.of("UTC")));
    }

    // Method to override with a chosen time and date
    public String getFormattedDateTime(LocalDateTime customDateTime) {
        return FORMATTER.format(customDateTime);
    }

    // for testing
    public static void main(String[] args) {
        DateUtil dateUtil = new DateUtil();
        // Get current date-time
        System.out.println("Current Time: " + dateUtil.getCurrentDateTime());

        // Override with a chosen time & date
        LocalDateTime customDateTime = LocalDateTime.of(2025, 4, 20, 23, 45, 0);
        System.out.println("Custom Time: " + dateUtil.getFormattedDateTime(customDateTime));
    }
}
