package com.learning;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VuePageController {

    @GetMapping({"/ui", "/ui/"})
    public String ui() {
        return "forward:/ui/index.html";
    }
}
