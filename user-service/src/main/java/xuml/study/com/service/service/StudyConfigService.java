package xuml.study.com.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xuml.study.com.model.domain.StudyConfig;

public interface StudyConfigService extends IService<StudyConfig> {

    StudyConfig getByCfgKey(String cfgKey);
}
