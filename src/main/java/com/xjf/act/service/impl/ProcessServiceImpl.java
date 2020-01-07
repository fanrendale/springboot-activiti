package com.xjf.act.service.impl;

import com.xjf.act.core.exception.BusinessException;
import com.xjf.act.core.exception.ProcessParamException;
import com.xjf.act.dto.param.ProcessParam;
import com.xjf.act.dto.process.AuthService;
import com.xjf.act.dto.response.ProcessHistoryResponse;
import com.xjf.act.dto.response.ProcessResponse;
import com.xjf.act.entity.WorkFlow;
import com.xjf.act.entity.WorkFlowStep;
import com.xjf.act.mapper.WorkFlowMapper;
import com.xjf.act.service.ProcessService;
import com.xjf.act.util.ListUtil;
import com.xjf.act.util.UUIDUtil;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
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
    @Autowired
    private WorkFlowMapper workFlowMapper;
    @Autowired
    private AuthService authService;


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

    @Override
    public Task getTaskById(String taskId) {
        if (StringUtils.isAnyBlank(taskId)) {
            throw new ProcessParamException("流程参数错误：任务ID不能为空");
        }

        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    @Override
    public Object getVariableByVarName(String taskId, String candidateVariableName) {
        if (StringUtils.isAnyBlank(taskId, candidateVariableName)) {
            throw new ProcessParamException("流程参数错误：任务ID,变量名称不能为空");
        }

        return taskService.getVariable(taskId, candidateVariableName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProcessDeployment(WorkFlow workFlow, List<WorkFlowStep> stepList) {
        //1. 创建BPMN模型对象
        BpmnModel bpmnModel = new BpmnModel();

        //2. 创建一个流程定义
        Process process = new Process();
        //此流程定义id为数据库中的流程定义key，在开启流程时使用这个key，回存到自定义流程业务表中
        process.setId("_" + UUIDUtil.getUUID());
        workFlow.setKey(process.getId());
        workFlowMapper.updateById(workFlow);
        process.setName("customProcess");
        bpmnModel.addProcess(process);

        //3. 开始事件
        process.addFlowElement(createStartEvent());

        //4. 中间节点生成
        stepList.forEach(step -> {
            if (1 == step.getOrder()){
                if (step.getType() != 1){
                    throw new BusinessException("自定义流程的第一个步骤必须是填写类型");
                }

                //开始步骤填写
                process.addFlowElement(createUserTask("userTask-" + step.getOrder(), step.getName()));
            }else {
                if (1 == step.getType()){
                    //中间的填写节点
                    process.addFlowElement(createUserTask("userTask-" + step.getOrder(), step.getName()));

                } else if (2 == step.getType()){
                    //审批节点
                    process.addFlowElement(createUserTask("userTask-" + step.getOrder(), step.getName()));

                    //互斥网关
                    process.addFlowElement(createExclusiveGateway("exclusiveGateway-" + step.getOrder()));
                }
            }
        });

        //5. 结束事件
        process.addFlowElement(createEndEvent());

        //6. 连线
        for (int i = 0; i < stepList.size(); i++) {
            WorkFlowStep step = stepList.get(i);
            if (1 == step.getOrder()){
                //第一步
                process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "startEvent", "userTask-" + step.getOrder(),null,null));
            }else {
                if (2 == step.getType()){
                    //审批节点
                    process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "userTask-" + step.getOrder(), "exclusiveGateway-" + step.getOrder(), null,null));

                    //驳回的线
                    process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "exclusiveGateway-" + step.getOrder(), "userTask-" + (step.getOrder()-1), "驳回", "${flag==0}"));

                    //上一个任务到当前任务的线（如果上一个任务和当前任务都是审批节点时不需要）
                    if (1 == stepList.get(i-1).getType()) {
                        process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "userTask-" + (step.getOrder() - 1), "userTask-" + step.getOrder(), null, null));
                    }

                    //如果当前步骤是最后一步则直接连结束事件，不然就连下一个步骤
                    String targetRef;
                    if ((i + 1) < stepList.size()) {
                        targetRef = "userTask-" + (step.getOrder() + 1);
                    }else {
                        targetRef = "endEvent";
                    }
                    process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "exclusiveGateway-" + step.getOrder(), targetRef, "同意", "${flag==1}"));
                }else if (1 == step.getType()){
                    //填写节点
                    WorkFlowStep lastStep = stepList.get(i-1);
                    if (1 == lastStep.getType()){
                        //上一个节点是填写节点
                        process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "userTask-" + (step.getOrder() - 1), "userTask-" + step.getOrder(), null, null));
                    } //上一个节点是审批节点,不用做任何事


                    //如果当前节点是最后一个节点，则连接结束节点
                    if ((i + 1) == stepList.size()) {
                        process.addFlowElement(createSequenceFlow("_" + UUIDUtil.getUUID(), "userTask-" + step.getOrder(), "endEvent", null, null));
                    }
                }
            }
        }

        //7. 生成BPMN自动布局,根据流程属性的定义自动生成对应的位置等信息
        System.out.println("bpmn的值：" + bpmnModel);
        new BpmnAutoLayout(bpmnModel).execute();

        //8. 发布
        Deployment deployment = repositoryService.createDeployment()
                .addBpmnModel(process.getId() + ".bpmn", bpmnModel)
                .name(process.getId()+"_customDeployment")
                .deploy();

        //9. 根据流程资源导出bpmn文件，数据库中同时会生成png资源以及流程定义表的数据
        InputStream processBpmn =
                repositoryService.getResourceAsStream(deployment.getId(),  process.getId() + ".bpmn");
        try {
            //项目相对路径
            String projectPath = System.getProperty("user.dir");
            //bpmn资源路径
            String path = projectPath + File.separator +"src" + File.separator + "main" + File.separator + "resources" +File.separator +"processes" + File.separator + process.getId() + ".bpmn";
            FileUtils.copyInputStreamToFile(processBpmn, new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void suspendProcessInstanceById(String processInstanceId) {
        if (StringUtils.isAnyBlank(processInstanceId)) {
            throw new ProcessParamException("流程参数错误：流程实例ID不能为空");
        }

        //判断流程是否已经结束
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (Objects.isNull(processInstance)){
            throw new BusinessException("流程已经结束，不能终止");
        }

        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    public void addCandidateUser(String taskId, String userId) {
        if (StringUtils.isAnyBlank(taskId, userId)) {
            throw new ProcessParamException("流程参数错误：任务ID、用户ID不能为空");
        }

        taskService.addCandidateUser(taskId, userId);
    }

    @Override
    public List<HistoricTaskInstance> processStatus(String processInstanceId) {
        if (StringUtils.isAnyBlank(processInstanceId)) {
            throw new ProcessParamException("流程参数错误：流程实例ID不能为空");
        }

        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).list();

        return list;
    }

    /*======================================================封装的流程节点方法===start=============================================*/

    /**
     * 开始事件
     *
     * @return
     */
    private StartEvent createStartEvent(){
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");

        return startEvent;
    }

    /**
     * 结束事件
     *
     * @return
     */
    private EndEvent createEndEvent(){
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");

        return endEvent;
    }

    /**
     * 用户任务
     *
     * @param id 任务ID
     * @param name 任务名称
     * @return
     */
    private UserTask createUserTask(String id, String name){
        UserTask userTask = new UserTask();
        userTask.setId(id);
        userTask.setName(name);
        //任务的代理人都设置为同一个变量
        userTask.setAssignee("${assignee}");

        return userTask;
    }

    /**
     * 互斥网关
     *
     * @param id
     * @return
     */
    private ExclusiveGateway createExclusiveGateway(String id){
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);

        return exclusiveGateway;
    }

    /**
     * 连线
     *
     * @param id 连线ID
     * @param sourceRef 源元素ID
     * @param targetRef 目标元素ID
     * @return
     */
    private SequenceFlow createSequenceFlow(String id, String sourceRef, String targetRef, String name, String conditionExpression){
        SequenceFlow sequenceFlow = new SequenceFlow();
        sequenceFlow.setSourceRef(sourceRef);
        sequenceFlow.setTargetRef(targetRef);
        sequenceFlow.setId(id);
        if (StringUtils.isNotBlank(name)){
            sequenceFlow.setName(name);
        }
        if (StringUtils.isNotBlank(conditionExpression)) {
            sequenceFlow.setConditionExpression(conditionExpression);
        }

        return sequenceFlow;
    }

    /*======================================================封装的流程节点方法===end===============================================*/
}