package com.learning;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LedgerController {

    private static final Logger log = LoggerFactory.getLogger(LedgerController.class);
    private final LedgerService ledgerService;
    private final CurrentUser currentUser;
    private final ExcelExportService excelExportService;

    public LedgerController(LedgerService ledgerService, CurrentUser currentUser, ExcelExportService excelExportService) {
        this.ledgerService = ledgerService;
        this.currentUser = currentUser;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) String month, @RequestParam(defaultValue = "1") int page) {
        fillHome(model, currentUser.get(), month, page);
        return "home";
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) String month) {
        AppUser user = currentUser.get();
        var rows = ledgerService.listForExport(user.getId(), month);
        log.info("网页导出 username={} month={} count={}", user.getUsername(), month, rows.size());
        return excelExportService.download(excelExportService.ownBills(rows), "我的账单.xlsx");
    }

    @PostMapping("/add")
    public String add(@Valid LedgerForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            fillHome(model, currentUser.get(), null, 1);
            model.addAttribute("formError", firstError(bindingResult));
            return "home";
        }
        ledgerService.add(currentUser.get().getId(), form.getType(), form.getAmount(), form.getCategory(), form.getNote());
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id) {
        ledgerService.delete(currentUser.get().getId(), id);
        return "redirect:/";
    }

    @GetMapping("/edit")
    public String editPage(@RequestParam Long id, Model model) {
        model.addAttribute("entry", ledgerService.findOwn(currentUser.get().getId(), id));
        return "edit";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam Long id, @Valid LedgerForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("entry", ledgerService.findOwn(currentUser.get().getId(), id));
            model.addAttribute("formError", firstError(bindingResult));
            return "edit";
        }
        ledgerService.update(currentUser.get().getId(), id, form.getType(), form.getAmount(), form.getCategory(), form.getNote());
        return "redirect:/";
    }

    private void fillHome(Model model, AppUser user, String month, int page) {
        LedgerSummary summary = ledgerService.list(user.getId(), month, page, 5);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("admin", currentUser.isAdmin());
        model.addAttribute("entries", summary.getEntries());
        model.addAttribute("income", summary.getIncome());
        model.addAttribute("expense", summary.getExpense());
        model.addAttribute("balance", summary.getBalance());
        model.addAttribute("month", summary.getMonth());
        model.addAttribute("categoryStats", summary.getCategoryStats());
        model.addAttribute("page", summary.getPage());
        model.addAttribute("totalPages", summary.getTotalPages());
        model.addAttribute("totalCount", summary.getTotalCount());
    }

    private String firstError(BindingResult bindingResult) {
        FieldError error = bindingResult.getFieldError();
        return error == null ? "填写不正确" : error.getDefaultMessage();
    }
}
