package com.riskview.realty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/board")
public class BoardController {
    @GetMapping("/")
    public String board(Model model) {
        return "board";
    }

    /**
     * /board/about 페이지
     */
    @GetMapping("/about")
    public String about(Model model) {
        return "board/about";
    }

    /**
     * /board/analysis 페이지
     */
    @GetMapping("/analysis")
    public String analysis(Model model) {
        return "board/analysis";
    }

    /**
     * /board/community 페이지
     */
    @GetMapping("/community")
    public String community(Model model) {
        return "board/community";
    }

    /**
     * /board/news 페이지
     */
    @GetMapping("/news")
    public String news(Model model) {
        return "board/news";
    }
}
