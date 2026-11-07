package com.pdm.loan.service;

import java.math.BigDecimal;
import java.util.List;

public final class EligibilityChecker {

    public record Check(String rule, Boolean passed, String detail) {
    }

    private EligibilityChecker() {
    }

    public static List<Check> check(String rules, BigDecimal monthlyIncome) {
        return List.of(new Check("Automatic eligibility check", null, "Not implemented yet"));
    }
}
