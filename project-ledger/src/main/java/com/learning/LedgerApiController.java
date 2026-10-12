package com.learning;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entries")
public class LedgerApiController {

    private static final Logger log = LoggerFactory.getLogger(LedgerApiController.class);
    private final LedgerService ledgerService;
    private final CurrentUser currentUser;
    private final ExcelExportService excelExportService;

    public LedgerApiController(LedgerService ledgerService, CurrentUser currentUser, ExcelExportService excelExportService) {
        this.ledgerService = ledgerService;
        this.currentUser = currentUser;
        this.excelExportService = excelExportService;
    }

    @GetMapping
    public LedgerSummary list(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ledgerService.list(currentUser.get().getId(), month, page, size);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String month) {
        List<LedgerEntry> rows = ledgerService.listForExport(currentUser.get().getId(), month);
        log.info("接口导出 username={} month={} count={}", currentUser.get().getUsername(), month, rows.size());
        return excelExportService.download(excelExportService.ownBills(rows), "我的账单.xlsx");
    }

    @PostMapping
    public Map<String, Object> add(@Valid @RequestBody LedgerForm form) {
        ledgerService.add(currentUser.get().getId(), form.getType(), form.getAmount(), form.getCategory(), form.getNote());
        return Map.of("ok", true);
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @Valid @RequestBody LedgerForm form) {
        ledgerService.update(currentUser.get().getId(), id, form.getType(), form.getAmount(), form.getCategory(), form.getNote());
        return Map.of("ok", true);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        ledgerService.delete(currentUser.get().getId(), id);
        return Map.of("ok", true);
    }
}
