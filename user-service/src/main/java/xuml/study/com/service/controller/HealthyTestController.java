package xuml.study.com.service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xuml.study.com.common.dto.Result;
import xuml.study.com.common.exception.BusinessException;

import javax.websocket.server.PathParam;


@RestController
@RequestMapping("/healthy")
public class HealthyTestController {

    @GetMapping("/test")
    public Result<String> healthy() {

        return Result.success("success");
    }

    @GetMapping("/err/test/{code}")
    public Result<String> errTest(@PathVariable String code) {
        errTestMth(code);
        return Result.success("success");
    }

    private void errTestMth(String code) {
        switch (code) {
            case "1":
                throw new BusinessException("1001", "这是一个业务异常");
            case "2":
                throw new IllegalArgumentException("这是一个非法参数异常");
            case "3":
                throw new NullPointerException("这是一个空指针异常");
            default:
                throw new RuntimeException("这是一个未知异常");
        }
    }
}
