package com.learning;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionDemoController {

    private final LedgerService ledgerService;
    private final CurrentUser currentUser;

    public TransactionDemoController(LedgerService ledgerService, CurrentUser currentUser) {
        this.ledgerService = ledgerService;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/tx")
    public Map<String, Object> tx(@RequestParam(defaultValue = "false") boolean fail) {
        ledgerService.addTwoForDemo(currentUser.get().getId(), fail);
        return Map.of("ok", true);
    }
}
