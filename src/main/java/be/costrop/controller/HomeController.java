package be.costrop.controller;

import be.costrop.controller.dto.DeleteFileRequest;
import be.costrop.excel.ExcelFinderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ExcelFinderImpl excelFinder;

    @GetMapping
    String index(Model model) {
        log.info("Index page requested, starting search for excel files");
        final var excelsFoundOnDevice = excelFinder.getSearchResults();
        model.addAttribute("excelsFoundOnDevice", excelsFoundOnDevice);
        return "index";
    }

    @PostMapping("/delete")
    String delete(List<DeleteFileRequest> deleteFileRequests, Model model) {
        excelFinder.deleteFiles(deleteFileRequests);
        return index(model);
    }

}
