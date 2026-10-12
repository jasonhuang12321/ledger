package com.learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final LedgerService ledgerService;
    private final UserRepository userRepository;
    private final ExcelExportService excelExportService;

    public AdminController(LedgerService ledgerService, UserRepository userRepository, ExcelExportService excelExportService) {
        this.ledgerService = ledgerService;
        this.userRepository = userRepository;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/admin")
    public String admin(Authentication authentication, Model model, @RequestParam(required = false) String month, @RequestParam(defaultValue = "1") int page) {
        LedgerSummary summary = ledgerService.listAll(month, page, 10);
        Map<Long, String> names = usernames();
        model.addAttribute("username", authentication.getName());
        model.addAttribute("summary", summary);
        model.addAttribute("income", summary.getIncome());
        model.addAttribute("expense", summary.getExpense());
        model.addAttribute("balance", summary.getBalance());
        model.addAttribute("month", summary.getMonth());
        model.addAttribute("page", summary.getPage());
        model.addAttribute("totalPages", summary.getTotalPages());
        model.addAttribute("totalCount", summary.getTotalCount());
        model.addAttribute("entries", summary.getEntries());
        model.addAttribute("usernames", names);
        return "admin";
    }

    @GetMapping("/admin/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String month) {
        List<LedgerEntry> rows = ledgerService.listAllForExport(month);
        return excelExportService.download(excelExportService.allBills(rows, usernames()), "全部账单.xlsx");
    }

    private Map<Long, String> usernames() {
        Map<Long, String> names = new HashMap<>();
        for (AppUser user : userRepository.findAll()) {
            names.put(user.getId(), user.getUsername());
        }
        return names;
    }
}
