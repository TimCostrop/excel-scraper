package be.costrop.controller;

import be.costrop.excel.ExcelFinderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ExcelFinderImpl excelFinder;

    @GetMapping
    String index(Model model) {
        log.info("Index page requested, starting search for excel files");
        final var excelsFoundOnDevice = excelFinder.search();
        model.addAttribute("excelsFoundOnDevice", excelsFoundOnDevice);
        return "index";
    }
}
