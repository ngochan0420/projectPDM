package com.pdm.loan.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EligibilityChecker {

    public record Check(String rule, Boolean passed, String detail) {
        /** passed == null means "needs a manual check". */
    }

    private static final Pattern MIN_INCOME =
            Pattern.compile("(?i)(?:min(?:imum)? )?income (?:from )?(\\d+(?:\\.\\d+)?)\\s*M");

    private EligibilityChecker() {
    }

    public static List<Check> check(String rules, BigDecimal monthlyIncome) {
        List<Check> checks = new ArrayList<>();
        if (rules == null || rules.isBlank()) {
            return checks;
        }
        for (String raw : rules.split(";")) {
            String rule = raw.trim();
            if (rule.isEmpty()) {
                continue;
            }
            Matcher m = MIN_INCOME.matcher(rule);
            if (m.find()) {
                BigDecimal min = new BigDecimal(m.group(1)).multiply(BigDecimal.valueOf(1_000_000));
                BigDecimal income = monthlyIncome == null ? BigDecimal.ZERO : monthlyIncome;
                boolean ok = income.compareTo(min) >= 0;
                checks.add(new Check(rule, ok, "Declared income " + Fmt.money(income) + " VND / month"));
            } else {
                checks.add(new Check(rule, null, "Check manually"));
            }
        }
        return checks;
    }
}
