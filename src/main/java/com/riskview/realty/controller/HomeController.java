package com.riskview.realty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    /**
     * 홈 페이지
     * @param model
     * @return 홈 페이지
     */
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }
}