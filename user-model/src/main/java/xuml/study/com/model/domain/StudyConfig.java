package xuml.study.com.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName study_config
 */
@TableName(value ="study_config")
@Data
public class StudyConfig implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String cfgValue;

    /**
     * 
     */
    private String cfgKey;

    /**
     * 父id
     */
    private Long parentId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 描述
     */
    private String cfgDesc;

    /**
     * 是否删除 0-未删；1-删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}