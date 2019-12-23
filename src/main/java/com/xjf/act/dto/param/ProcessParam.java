package com.xjf.act.dto.param;

import lombok.Data;

import java.util.Map;
import java.util.Objects;

/**
 * 流程相关的参数
 *
 * @Author: xjf
 * @Since: 2019/12/18 11:06
 */
@Data
public class ProcessParam {

    /**
     * 流程定义的key，bpmn文件的id
     */
    private String processDefinitionKey;

    /**
     * 业务键，流程关联业务
     */
    private String businessKey;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户组ID
     */
    private String groupId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 业务主键ID
     */
    private Long id;

    /**
     * 是否同意：0-不通过，1-通过
     */
    private Integer flag;

    /**
     * 反馈意见
     */
    private String feedback;

    /**
     * 分页起始页: activiti的起始页为0
     */
    private Integer pageNum = 0;

    /**
     * 分页容量
     */
    private Integer pageSize = 10;

    public void setPageNum(Integer pageNum) {
        if (Objects.isNull(pageNum)){
            this.pageNum = 0;
        }else {
            //在controller设置时，跟mybatis的保持一致，可以值起始页为1，此处减一
            this.pageNum = pageNum-1;
        }
    }

    public void setPageSize(Integer pageSize) {
        if (Objects.isNull(pageSize)){
            this.pageSize = 10;
        }else {
            this.pageSize = pageSize;
        }
    }
}
