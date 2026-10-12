package com.learning;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public LedgerSummary list(Long userId, String month) {
        return list(userId, month, 1, 5);
    }

    public LedgerSummary list(Long userId, String month, int page, int pageSize) {
        return summarize(ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId), month, page, pageSize);
    }

    public LedgerSummary listAll(String month, int page, int pageSize) {
        return summarize(ledgerRepository.findAllByOrderByCreatedAtDesc(), month, page, pageSize);
    }

    public List<LedgerEntry> listForExport(Long userId, String month) {
        return filterMonth(ledgerRepository.findByUserIdOrderByCreatedAtDesc(userId), month);
    }

    public List<LedgerEntry> listAllForExport(String month) {
        return filterMonth(ledgerRepository.findAllByOrderByCreatedAtDesc(), month);
    }

    private List<LedgerEntry> filterMonth(List<LedgerEntry> entries, String month) {
        if (month == null || month.isBlank()) {
            return entries;
        }
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntry entry : entries) {
            if (entry.getCreatedAt() != null && entry.getCreatedAt().toString().startsWith(month)) {
                result.add(entry);
            }
        }
        return result;
    }

    private LedgerSummary summarize(List<LedgerEntry> entries, String month, int page, int pageSize) {
        entries = filterMonth(entries, month);
        double income = 0;
        double expense = 0;
        Map<String, Double> byCategory = new LinkedHashMap<>();
        for (LedgerEntry entry : entries) {
            if ("收入".equals(entry.getType())) {
                income += entry.getAmount();
            } else {
                expense += entry.getAmount();
                byCategory.merge(entry.getCategory(), entry.getAmount(), Double::sum);
            }
        }
        List<CategoryStat> stats = new ArrayList<>();
        byCategory.forEach((category, amount) -> stats.add(new CategoryStat(category, amount)));
        int totalCount = entries.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalCount / (double) pageSize));
        int safePage = Math.min(Math.max(page, 1), totalPages);
        int from = (safePage - 1) * pageSize;
        int to = Math.min(from + pageSize, totalCount);
        List<LedgerEntry> pageRows = from >= totalCount ? List.of() : entries.subList(from, to);
        return new LedgerSummary(
                pageRows,
                income,
                expense,
                income - expense,
                month == null ? "" : month,
                stats,
                safePage,
                totalPages,
                totalCount);
    }

    public void add(Long userId, String type, double amount, String category, String note) {
        if (!"收入".equals(type) && !"支出".equals(type)) {
            throw new BusinessException("类型只能是收入或支出");
        }
        if (amount <= 0) {
            throw new BusinessException("金额必须大于 0");
        }
        if (category == null || category.isBlank()) {
            throw new BusinessException("分类不能为空");
        }
        ledgerRepository.save(new LedgerEntry(userId, type, amount, category, note == null ? "" : note));
    }

    public LedgerEntry findOwn(Long userId, Long id) {
        LedgerEntry entry = ledgerRepository.findById(id).orElseThrow(() -> new BusinessException("记录不存在"));
        if (!userId.equals(entry.getUserId())) {
            throw new BusinessException("不能操作别人的账单");
        }
        return entry;
    }

    public void update(Long userId, Long id, String type, double amount, String category, String note) {
        LedgerEntry entry = findOwn(userId, id);
        if (!"收入".equals(type) && !"支出".equals(type)) {
            throw new BusinessException("类型只能是收入或支出");
        }
        entry.setType(type);
        entry.setAmount(amount);
        entry.setCategory(category);
        entry.setNote(note == null ? "" : note);
        ledgerRepository.save(entry);
    }

    public void delete(Long userId, Long id) {
        LedgerEntry entry = findOwn(userId, id);
        ledgerRepository.delete(entry);
    }

    @Transactional
    public void addTwoForDemo(Long userId, boolean failSecond) {
        add(userId, "支出", 1, "事务演示", "第一笔");
        if (failSecond) {
            throw new BusinessException("故意失败，两笔都应回滚");
        }
        add(userId, "支出", 1, "事务演示", "第二笔");
    }
}
