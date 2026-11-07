package com.pdm.loan.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Fmt {

    private static final DecimalFormat MONEY = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat RATE = new DecimalFormat("0.0#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);

    private Fmt() {
    }

    public static String money(Number n) {
        return n == null ? "—" : MONEY.format(n);
    }

    public static String vnd(Number n) {
        return n == null ? "—" : MONEY.format(n) + " VND";
    }

    public static String rate(BigDecimal r) {
        return r == null ? "—" : RATE.format(r) + "% / year";
    }

    public static String date(LocalDate d) {
        return d == null ? "—" : DATE.format(d);
    }

    public static String or(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    public static BigDecimal parseMoney(String text) {
        String t = text == null ? "" : text.replace(",", "").replace(" ", "").trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Please enter an amount.");
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a valid amount, e.g. 150,000,000");
        }
    }
}
