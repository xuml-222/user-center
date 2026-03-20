package xuml.study.com.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/healthy")
public class HealthyTestController {

    @GetMapping("/test")
    public String healthy() {

        return "success";
    }
}
