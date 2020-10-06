package net.ldcc.playground.controller;

import net.ldcc.playground.model.Member;
import net.ldcc.playground.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Controller
public class HomeController {
    private final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private MemberService memberService;

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public String home(Model model) throws SQLException {
        DatabaseMetaData dbMetaData = dataSource.getConnection().getMetaData();

        model.addAttribute("dbMetaData", dbMetaData);

        return "/home";
    }
}
