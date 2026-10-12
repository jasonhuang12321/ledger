package com.learning;

public class CategoryStat {
    private final String category;
    private final double amount;

    public CategoryStat(String category, double amount) {
        this.category = category;
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
}
