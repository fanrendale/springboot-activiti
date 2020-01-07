package com.xjf.act.dto.param;

import com.xjf.act.entity.WorkFlow;
import com.xjf.act.entity.WorkFlowStep;
import lombok.Data;

import java.util.List;

/**
 * 自定义流程的参数
 *
 * @Author: xjf
 * @Since: 2019/12/30 19:22
 */
@Data
public class WorkFlowParam {

    /**
     * 自定义流程的描述
     */
    private WorkFlow workFlow;

    /**
     * 自定义流程的步骤
     */
    private List<WorkFlowStep> stepList;
}
