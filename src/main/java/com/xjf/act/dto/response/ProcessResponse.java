package com.xjf.act.dto.response;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: xjf
 * @Since: 2019/12/19 11:54
 */
@Data
@ToString(callSuper = true)
public class ProcessResponse {

    /**
     * 业务数据对象
     */
    private Object businessObject;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 业务键
     */
    private String businessKey;
}
