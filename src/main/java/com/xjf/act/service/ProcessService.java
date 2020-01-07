package com.xjf.act.service;

import com.xjf.act.dto.param.ProcessParam;
import com.xjf.act.dto.response.ProcessHistoryResponse;
import com.xjf.act.dto.response.ProcessResponse;
import com.xjf.act.entity.WorkFlow;
import com.xjf.act.entity.WorkFlowStep;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 流程相关的服务
 * 特别注意：用户ID、用户组ID等必须设置为String类型
 *
 * @Author: xjf
 * @Since: 2019/12/18 9:51
 */
public interface ProcessService {

    /**
     * 使用流程定义的key启动流程实例
     *
     * businessKey的组成：流程定义的key + "-" + 业务数据id
     *
     * @param param
     * @return
     */
    ProcessInstance startProcessInstanceByKey(ProcessParam param);

    /**
     * 分页查询：根据用户id查询可领取的任务
     * 参数：userId：用户ID
     *      pageNum：分页起始页
     *      pageSize：分页容量
     *      processDefinitionKey:流程定义key，以此来分类查询
     *
     * @param param
     * @return
     */
    List<Task> availableTaskListByUser(ProcessParam param);

    /**
     * 根据用户id查询可领取的任务总数
     * @param userId
     * @return
     */
    long availableTaskCount(String userId);

    /**
     * 分页查询：根据用户组id查询可领取的任务
     *
     * @param param
     * @return
     */
    List<Task> availableTaskListByGroup(ProcessParam param);

    /**
     * 领取任务
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return
     */
    boolean claim(String userId, String taskId);

    /**
     * 分页查询：根据用户ID查询待办任务（已领取任务）
     *
     * @param param
     * @return
     */
    List<Task> toDoTaskListByUser(ProcessParam param);

    /**
     * 待办任务的总数
     *
     * @param userId
     * @return
     */
    long toDoTaskCount(String userId);

    /**
     * 完成任务
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param variables 任务参数
     * @return
     */
    boolean complete(String userId, String taskId, Map<String, Object> variables);

    /**
     * 根据流程实例ID查询当前流程图输出流
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    InputStream getDiagramInputStream(String processInstanceId);

    /**
     * 根据流程实例ID查询历史记录
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    List<ProcessHistoryResponse> historyList(String processInstanceId);

    /**
     * 通过流程实例ID获取业务键
     *
     * @param processInstanceId 流程实例ID
     * @return
     */
    String getBusinessKeyByProcessInstanceId(String processInstanceId);

    /**
     * 根据流程ID获取当前任务,一个任务的情况
     *
     * @param processInstanceId
     * @return
     */
    Task getOneTaskByProcessInstanceId(String processInstanceId);

    /**
     * 根据任务list批量获取对应的businessKey
     * @param list
     * @return
     */
    List<ProcessResponse> getProcessResponseByTask(List<Task> list);

    /**
     * 判断用户是否是任务的代理人
     *
     * @param userId
     * @param taskId
     * @return
     */
    boolean isUserAssigneeTask(String userId, String taskId);

    /**
     * 获取上个节点任务的代理人（驳回使用）
     * 该方法应该能优化
     *
     * @param userId
     * @param taskId
     * @return
     */
    String getLastTaskAssignee(String userId, String taskId);

    /**
     * 新增用户自定义流程
     *
     * @param workFlow 自定义流程描述
     * @param stepList 自定义流程步骤
     */
    void addProcessDeployment(WorkFlow workFlow, List<WorkFlowStep> stepList);

    /**
     * 根据任务ID查询任务
     * @param taskId
     * @return
     */
    Task getTaskById(String taskId);

    /**
     * 根据任务ID和变量名获取变量
     *
     * @param id
     * @param candidateVariableName
     * @return
     */
    Object getVariableByVarName(String id, String candidateVariableName);

    /**
     * 根据流程实例ID终止流程（挂起）
     *
     * @param processInstanceId 流程实例ID
     */
    void suspendProcessInstanceById(String processInstanceId);

    /**
     * 给任务设置候选人
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     */
    void addCandidateUser(String taskId, String userId);


    /**
     * 查询流程进度
     *
     * @param processInstanceId
     * @return
     */
    List<HistoricTaskInstance> processStatus(String processInstanceId);
}
