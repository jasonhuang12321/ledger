package com.learning;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(BusinessException ex, Model model, HttpServletRequest request) {
        if (isApi(request)) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldError() == null
                ? "参数不正确"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        if (isApi(request)) {
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public Object handleOther(Exception ex, Model model, HttpServletRequest request) {
        log.error("未处理异常 {}", request.getRequestURI(), ex);
        if (isApi(request)) {
            return ResponseEntity.internalServerError().body(Map.of("error", "系统出了点问题，请稍后再试"));
        }
        model.addAttribute("message", "系统出了点问题，请稍后再试");
        return "error";
    }

    private boolean isApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }
}
