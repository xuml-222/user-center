package xuml.study.com.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xuml.study.com.common.dto.Result;


@RestController
@RequestMapping("/healthy")
public class HealthyTestController {

    @GetMapping("/test")
    public Result<String> healthy() {

        return Result.success("success");
    }
}
