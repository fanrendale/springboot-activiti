package com.xjf.act.dto.response;

import lombok.Data;

import java.util.Date;

/**
 * 流程历史记录的返回实体
 *
 * @Author: xjf
 * @Since: 2019/12/19 16:19
 */
@Data
public class ProcessHistoryResponse {

    /**
     * 执行环节
     */
    private String step;

    /**
     * 执行人
     */
    private String executor;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 任务历时：单位s
     */
    private Long duration;

    /**
     * 任务评论
     */
    private String comment;
}
