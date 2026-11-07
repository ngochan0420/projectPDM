package com.pdm.loan.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class LoanCalculator {

    private LoanCalculator() {
    }

    public static BigDecimal monthlyPayment(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Term must be at least 1 month.");
        }
        double p = principal.doubleValue();
        double r = annualRatePercent == null ? 0 : annualRatePercent.doubleValue() / 12 / 100;
        double payment = r == 0 ? p / months : p * r / (1 - Math.pow(1 + r, -months));
        return BigDecimal.valueOf(payment).setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal interestFor(BigDecimal balance, BigDecimal annualRatePercent) {
        if (annualRatePercent == null) {
            return BigDecimal.ZERO;
        }
        return balance.multiply(annualRatePercent)
                .divide(BigDecimal.valueOf(1200), MathContext.DECIMAL64)
                .setScale(0, RoundingMode.HALF_UP);
    }
}
