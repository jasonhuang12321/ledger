package com.learning;

import java.util.List;

public class LedgerSummary {
    private final List<LedgerEntry> entries;
    private final double income;
    private final double expense;
    private final double balance;
    private final String month;
    private final List<CategoryStat> categoryStats;
    private final int page;
    private final int totalPages;
    private final int totalCount;

    public LedgerSummary(
            List<LedgerEntry> entries,
            double income,
            double expense,
            double balance,
            String month,
            List<CategoryStat> categoryStats,
            int page,
            int totalPages,
            int totalCount) {
        this.entries = entries;
        this.income = income;
        this.expense = expense;
        this.balance = balance;
        this.month = month;
        this.categoryStats = categoryStats;
        this.page = page;
        this.totalPages = totalPages;
        this.totalCount = totalCount;
    }

    public List<LedgerEntry> getEntries() {
        return entries;
    }

    public double getIncome() {
        return income;
    }

    public double getExpense() {
        return expense;
    }

    public double getBalance() {
        return balance;
    }

    public String getMonth() {
        return month;
    }

    public List<CategoryStat> getCategoryStats() {
        return categoryStats;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
