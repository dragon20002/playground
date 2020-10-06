package net.ldcc.playground.controller;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class BaseErrorController extends AbstractErrorController {
    private static final String ERROR_PATH = "/error";

    public BaseErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    public BaseErrorController(ErrorAttributes errorAttributes, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @GetMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Map<String, Object> errorObj = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        model.addAllAttributes(errorObj);

        return "/error";
    }

}
