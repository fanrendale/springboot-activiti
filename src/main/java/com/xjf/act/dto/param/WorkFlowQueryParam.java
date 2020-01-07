package com.xjf.act.dto.param;

import com.xjf.act.dto.param.base.BaseParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: xjf
 * @Since: 2020/1/2 14:26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class WorkFlowQueryParam extends BaseParam {

    /**
     * 自定义流程的名称
     */
    private String name;
}
