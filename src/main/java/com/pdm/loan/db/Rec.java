package com.pdm.loan.db;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record Rec(Map<String, Object> values) {

    public Object get(String column) {
        if (!values.containsKey(column)) {
            throw new IllegalArgumentException("No column " + column);
        }
        return values.get(column);
    }

    public String s(String column) {
        Object v = get(column);
        return v == null ? null : v.toString();
    }

    public BigDecimal n(String column) {
        Object v = get(column);
        if (v == null) {
            return null;
        }
        return v instanceof BigDecimal b ? b : new BigDecimal(v.toString());
    }

    public int i(String column) {
        Object v = get(column);
        return v == null ? 0 : ((Number) v).intValue();
    }

    public Integer iOrNull(String column) {
        Object v = get(column);
        return v == null ? null : ((Number) v).intValue();
    }

    public boolean b(String column) {
        Object v = get(column);
        if (v == null) {
            return false;
        }
        if (v instanceof Boolean bool) {
            return bool;
        }
        return ((Number) v).intValue() != 0;
    }

    public LocalDate d(String column) {
        Object v = get(column);
        if (v == null) {
            return null;
        }
        if (v instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        if (v instanceof LocalDate ld) {
            return ld;
        }
        return LocalDate.parse(v.toString().substring(0, 10));
    }
}
