package com.learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final LedgerService ledgerService;
    private final UserRepository userRepository;
    private final ExcelExportService excelExportService;

    public AdminApiController(LedgerService ledgerService, UserRepository userRepository, ExcelExportService excelExportService) {
        this.ledgerService = ledgerService;
        this.userRepository = userRepository;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/entries")
    public Map<String, Object> entries(
            @RequestParam(required = false) String month,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<Long, String> usernames = new HashMap<>();
        for (AppUser user : userRepository.findAll()) {
            usernames.put(user.getId(), user.getUsername());
        }
        return Map.of(
                "summary", ledgerService.listAll(month, page, size),
                "usernames", usernames
        );
    }

    @GetMapping("/entries/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String month) {
        Map<Long, String> usernames = new HashMap<>();
        for (AppUser user : userRepository.findAll()) {
            usernames.put(user.getId(), user.getUsername());
        }
        List<LedgerEntry> rows = ledgerService.listAllForExport(month);
        return excelExportService.download(excelExportService.allBills(rows, usernames), "全部账单.xlsx");
    }
}
