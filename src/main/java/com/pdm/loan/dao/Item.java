package com.pdm.loan.dao;

/** One combo box option: stores the id but shows the label. */
public record Item(String id, String label) {
    @Override
    public String toString() {
        return id + " - " + label;
    }
}
