package com.numbericsuserportal.kintsugi.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public final class FilingPeriodUtil {

    private static final DateTimeFormatter DUE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    private FilingPeriodUtil() {
    }

    public static Map<String, Object> currentQuarter(LocalDate asOf) {
        int year = asOf.getYear();
        int month = asOf.getMonthValue();
        int quarter = (month - 1) / 3 + 1;

        LocalDate start = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate end = start.plusMonths(3).minusDays(1);
        LocalDate due = dueDateForQuarter(year, quarter);

        String label = "Q" + quarter + " " + year;
        String rangeLabel = monthShort(start.getMonth()) + " – " + monthShort(end.getMonth());

        return Map.of(
                "quarter", quarter,
                "year", year,
                "label", label,
                "rangeLabel", rangeLabel,
                "periodStart", start.toString(),
                "periodEnd", end.toString(),
                "dueDate", due.toString(),
                "dueDateLabel", DUE_FMT.format(due),
                "daysRemaining", Math.max(0, due.toEpochDay() - asOf.toEpochDay())
        );
    }

    public static Map<String, Object> nextQuarterAfter(LocalDate periodEnd) {
        LocalDate nextStart = periodEnd.plusDays(1);
        return currentQuarter(nextStart);
    }

    private static LocalDate dueDateForQuarter(int year, int quarter) {
        return switch (quarter) {
            case 1 -> LocalDate.of(year, Month.APRIL, 20);
            case 2 -> LocalDate.of(year, Month.JULY, 20);
            case 3 -> LocalDate.of(year, Month.OCTOBER, 20);
            default -> LocalDate.of(year + 1, Month.JANUARY, 20);
        };
    }

    private static String monthShort(Month month) {
        return month.name().substring(0, 1) + month.name().substring(1, 3).toLowerCase(Locale.US);
    }
}
