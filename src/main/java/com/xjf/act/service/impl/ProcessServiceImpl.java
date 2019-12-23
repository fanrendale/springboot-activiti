package com.xjf.act.service.impl;

import com.xjf.act.core.exception.BusinessException;
import com.xjf.act.core.exception.ProcessParamException;
import com.xjf.act.dto.param.ProcessParam;
import com.xjf.act.dto.response.ProcessHistoryResponse;
import com.xjf.act.dto.response.ProcessResponse;
import com.xjf.act.service.ProcessService;
import com.xjf.act.util.ListUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程相关服务的实现类
 *
 * @Author: xjf
 * @Since: 2019/12/18 9:51
 */
@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private HistoryService historyService;


    @Override
    public ProcessInstance startProcessInstanceByKey(ProcessParam param) {
        if (StringUtils.isAnyBlank(param.getUserId(), param.getProcessDefinitionKey(), param.getBusinessKey())) {
            throw new ProcessParamException("流程参数错误：用户ID、流程定义key、业务键值不能为空");
        }
        System.out.println(param);
        //设置流程实例启动人
        identityService.setAuthenticatedUserId(param.getUserId());
        return runtimeService.startProcessInstanceByKey(param.getProcessDefinitionKey(), param.getBusinessKey(), param.getVariables());
    }

    @Override
    public List<Task> availableTaskListByUser(ProcessParam param) {
        if (StringUtils.isBlank(param.getUserId())) {
            throw new ProcessParamException("流程参数错误：用户ID不能为空");
        }

        return taskService.createTaskQuery().taskCandidateUser(param.getUserId()).orderByTaskCreateTime().desc().listPage(param.getPageNum(), param.getPageSize());
    }

    @Override
    public long availableTaskCount(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new ProcessParamException("流程参数错误：用户ID不能为空");
        }
        return taskService.createTaskQuery().taskCandidateUser(userId).count();
    }

    @Override
    public List<Task> availableTaskListByGroup(ProcessParam param) {
        if (StringUtils.isBlank(param.getGroupId())) {
            throw new ProcessParamException("流程参数错误：用户组ID不能为空");
        }

        return taskService.createTaskQuery().taskCandidateGroup(param.getGroupId()).orderByTaskCreateTime().desc().listPage(param.getPageNum(), param.getPageSize());
    }

    @Override
    public boolean claim(String userId, String taskId) {
        if (StringUtils.isAnyBlank(userId, taskId)) {
            throw new ProcessParamException("流程参数错误：用户ID、任务ID不能为空");
        }

        //判断当前用户是否能领取该任务
        Task tmpTask = taskService.createTaskQuery().taskCandidateUser(userId).taskId(taskId).singleResult();
        if (Objects.isNull(tmpTask)){
            throw new BusinessException("当前用户不能领取该任务");
        }

        //判断任务是否已经被领取了
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (StringUtils.isNotBlank(task.getAssignee())) {
            throw new BusinessException("任务已经被领取过了");
        }

        taskService.claim(taskId, userId);

        return true;
    }

    @Override
    public List<Task> toDoTaskListByUser(ProcessParam param) {
        if (StringUtils.isBlank(param.getUserId())) {
            throw new ProcessParamException("流程参数错误：用户ID不能为空");
        }

        return taskService.createTaskQuery().taskAssignee(param.getUserId()).orderByTaskCreateTime().desc().listPage(param.getPageNum(), param.getPageSize());
    }

    @Override
    public long toDoTaskCount(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new ProcessParamException("流程参数错误：用户ID不能为空");
        }

        return taskService.createTaskQuery().taskAssignee(userId).count();
    }

    @Override
    public boolean complete(String userId, String taskId, Map<String, Object> variables) {
        if (StringUtils.isAnyBlank(taskId, userId)) {
            throw new ProcessParamException("流程参数错误：任务ID、用户ID不能为空");
        }

        Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(userId).singleResult();

        if (Objects.nonNull(variables)) {
            taskService.addComment(taskId, task.getProcessInstanceId(), Optional.ofNullable((String)variables.get("comment")).orElse(""));
        }

        if (Objects.nonNull(task)) {
            if (Objects.nonNull(variables) && !variables.isEmpty()) {
                taskService.complete(taskId, variables);
                return true;
            } else {
                taskService.complete(taskId);
                return true;
            }
        }

        return false;
    }

    /*===============================================================流程图==========start=========================================================*/

    @Override
    public InputStream getDiagramInputStream(String processInstanceId) {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new ProcessParamException("流程参数错误：流程实例ID不能为空");
        }

        //查询流程实例
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        // 查询流程实例
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(pi.getProcessDefinitionId()).singleResult();
        // 获取BPMN模型对象
        BpmnModel model = repositoryService.getBpmnModel(pd.getId());
        // 定义使用宋体
        String fontName = "宋体";
        // 获取流程实实例当前点的节点，需要高亮显示
        List<String> currentActs = runtimeService.getActiveActivityIds(pi.getId());
        // BPMN模型对象、图片类型、显示的节点
        InputStream is = this.processEngine
                .getProcessEngineConfiguration()
                .getProcessDiagramGenerator()
                .generateDiagram(model, "png", currentActs, new ArrayList<String>(),
                        fontName, fontName, fontName, null, 1.0);
        return is;
    }

    /*===============================================================流程图==========end=========================================================*/

    @Override
    public List<ProcessHistoryResponse> historyList(String processInstanceId) {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new ProcessParamException("流程参数错误：流程实例ID不能为空");
        }

        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        //开始时间正序排序
        list.sort((Comparator.comparing(HistoricActivityInstance::getStartTime)));

        List<ProcessHistoryResponse> responseList = new ArrayList<>();

        list.forEach(history -> {
            ProcessHistoryResponse response = new ProcessHistoryResponse();

            response.setStep(history.getActivityName());
            if (StringUtils.isNotBlank(history.getAssignee())) {
                response.setExecutor(identityService.createUserQuery().userId(history.getAssignee()).singleResult().getLastName());
            }else {
                response.setExecutor("无");
            }
            response.setStartTime(history.getStartTime());
            response.setEndTime(history.getEndTime());
            response.setDuration(history.getDurationInMillis() / 1000);
            List<Comment> taskComments = taskService.getTaskComments(history.getTaskId());
            if (ListUtil.notEmpty(taskComments)){
                //获取评论
                response.setComment(taskComments.stream().map(Comment::getFullMessage).collect(Collectors.joining(",")));
            }
            responseList.add(response);
        });

        return responseList;
    }

    @Override
    public String getBusinessKeyByProcessInstanceId(String processInstanceId) {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new ProcessParamException("流程参数错误：流程实例ID不能为空");
        }

        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getBusinessKey();
    }

    @Override
    public Task getOneTaskByProcessInstanceId(String processInstanceId) {
        if (StringUtils.isBlank(processInstanceId)) {
            throw new ProcessParamException("流程参数错误：流程实例ID不能为空");
        }

        return taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public List<ProcessResponse> getProcessResponseByTask(List<Task> taskList) {
        if (!ListUtil.notEmpty(taskList)){
            throw new ProcessParamException("流程参数错误：任务List不能为空");
        }

        //根据任务list获取对应的流程实例，然后再获取业务键
        return taskList.stream().map(task -> {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            ProcessResponse processResponse = new ProcessResponse();
            processResponse.setTaskId(task.getId());
            processResponse.setBusinessKey(processInstance.getBusinessKey());
            return processResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isUserAssigneeTask(String userId, String taskId) {
        if (StringUtils.isAnyBlank(userId,taskId)){
            throw new ProcessParamException("流程参数错误：用户ID、任务ID不能为空");
        }

        Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(userId).singleResult();

        return !Objects.isNull(task);
    }

    @Override
    public String getLastTaskAssignee(String userId, String taskId) {
        // TODO: 2019/12/21 该方法应该能优化
        if (StringUtils.isAnyBlank(taskId, userId)) {
            throw new ProcessParamException("流程参数错误：任务ID、用户ID不能为空");
        }

        Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(userId).singleResult();

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .orderByTaskCreateTime().desc()
                .processInstanceId(task.getProcessInstanceId())
                .list();

        if (ListUtil.notEmpty(list)){
            //上一个任务节点的代理人(是第二个，第一个是当前任务)
            return list.get(1).getAssignee();
        }

        return null;
    }

}
