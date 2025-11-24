package com.feedback.web.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler mapping common exceptions to error views.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Treat common not-found patterns as 404 to avoid adding custom exceptions everywhere.
     */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        // Heuristic: many services throw IllegalArgumentException with message containing "not found"
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.toLowerCase().contains("not found")) {
            model.addAttribute("message", message);
            return "error/404";
        }
        // If it's not a not-found case, treat as 500
        return handleGenericInternal(ex, model);
    }

    /**
     * Access denied (Spring Security) → 403 page.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }

    /**
     * Fallback generic handler → 500 page.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        return handleGenericInternal(ex, model);
    }

    private String handleGenericInternal(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("message", ex.getMessage());
        return "error/500";
    }
}
