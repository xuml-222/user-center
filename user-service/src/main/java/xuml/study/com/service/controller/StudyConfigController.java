package xuml.study.com.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xuml.study.com.model.domain.StudyConfig;
import xuml.study.com.service.service.StudyConfigService;

import javax.websocket.server.PathParam;

@RestController
@RequestMapping("/study/config")
public class StudyConfigController {

    @Autowired
    private StudyConfigService studyConfigService;

    @GetMapping("/getByCfgKey")
    public StudyConfig getByCfgKey(@PathParam("cfgKey") String cfgKey) {
        if(!StringUtils.hasText(cfgKey)) {
            return null;
        }
        return studyConfigService.getByCfgKey(cfgKey);
    }
}
