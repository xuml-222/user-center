package xuml.study.com.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import xuml.study.com.model.domain.StudyConfig;
import org.springframework.stereotype.Service;
import xuml.study.com.service.mapper.StudyConfigMapper;
import xuml.study.com.service.service.StudyConfigService;

/**
 * @author xuml
 * @description 针对表【study_config】的数据库操作Service实现
 * @createDate 2025-06-04 14:26:39
 */
@Slf4j
@Service
public class StudyConfigServiceImpl extends ServiceImpl<StudyConfigMapper, StudyConfig>
        implements StudyConfigService {

    @Override
    public StudyConfig getByCfgKey(String cfgKey) {
        log.info("请求参数：{}", cfgKey);
        return this.baseMapper.selectOne(new LambdaQueryWrapper<StudyConfig>()
                .eq(StudyConfig::getCfgKey, cfgKey)
                .eq(StudyConfig::getIsDelete, 0)
                .last("limit 1"));
    }
}




