package com.xjf.act.controller;

import com.alibaba.fastjson.JSON;
import com.xjf.act.controller.base.BaseController;
import com.xjf.act.core.exception.BusinessException;
import com.xjf.act.core.response.ReturnMsg;
import com.xjf.act.core.response.ReturnResult;
import com.xjf.act.dto.param.ProcessParam;
import com.xjf.act.dto.param.WorkFlowParam;
import com.xjf.act.dto.response.ProcessHistoryResponse;
import com.xjf.act.entity.WorkFlow;
import com.xjf.act.entity.WorkFlowStep;
import com.xjf.act.service.ProcessService;
import com.xjf.act.service.WorkFlowService;
import com.xjf.act.service.WorkFlowStepService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流程的公共服务类
 *
 * @Author: xjf
 * @Since: 2019/12/16 14:06
 */
@RestController
@RequestMapping("/process")
@Slf4j
public class ProcessController extends BaseController {

    @Autowired
    private ProcessService processService;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkFlowStepService workFlowStepService;

    /**
     * 领取任务
     *
     * @param taskId
     * @return
     */
    @PostMapping("/claim")
    public ReturnResult claim(@RequestParam("taskId") String taskId){
        log.info("========================领取任务===开始：参数:param[taskId:{}]=============================", JSON.toJSONString(taskId));

        if (StringUtils.isBlank(taskId)){
            return ReturnResult.error("领取任务的taskId不能为空", ReturnMsg.ERROR1.getCode(), ReturnMsg.ERROR1.getMsg());
        }

        //获取当前登录用户
        String userId = String.valueOf(this.getCurrentUserId());
        processService.claim(userId, taskId);

        log.info("========================领取任务===成功=============================");
        return ReturnResult.success();
    }

    /**
     * 完成任务(当完成任务后的当前任务，候选人只有一个人时，自动给他领取任务）
     *
     * @param param
     * @return
     */
    @PostMapping("/complete")
    public ReturnResult complete(@RequestBody ProcessParam param){
        log.info("========================完成任务===开始：参数:param[{}]=============================", JSON.toJSONString(param));

        //当前登录用户
        String userId = String.valueOf(this.getCurrentUserId());

        //1.判断当前任务是否属于登录用户
        if (!processService.isUserAssigneeTask(userId, param.getTaskId())) {
            throw new BusinessException("当前用户不是此任务的代理人");
        }

        Task task = processService.getTaskById(param.getTaskId());
        processService.complete(userId,param.getTaskId(),param.getVariables());

        //再次查询当前任务
        if (StringUtils.isNotBlank(param.getCandidateVariableName())) {
            Task currentTask = processService.getOneTaskByProcessInstanceId(task.getProcessInstanceId());
            if (Objects.nonNull(currentTask)) {
                Object variable = processService.getVariableByVarName(currentTask.getId(), param.getCandidateVariableName());
                if (Objects.nonNull(variable)) {
                    String candidateUser = (String) variable;
                    //给候选人自动领取任务
                    if (processService.claim(candidateUser, currentTask.getId())) {
                        log.info("完成任务后，给下一任务的候选人自动领取任务成功");
                    }
                }
            }
        }

        log.info("========================完成任务===成功=============================");
        return ReturnResult.success();
    }

    /**
     * 查询流程历史
     *
     * @param processInstanceId
     * @return
     */
    @GetMapping("/processHistory")
    public ReturnResult processHistory(@RequestParam("processInstanceId") String processInstanceId){
        log.info("========================查询流程历史===开始：参数:processInstanceId[{}]=============================", JSON.toJSONString(processInstanceId));

        if (StringUtils.isBlank(processInstanceId)){
            return ReturnResult.error("流程实例ID不能为空", ReturnMsg.ERROR1.getCode(), ReturnMsg.ERROR1.getMsg());
        }

        List<ProcessHistoryResponse> historyList = processService.historyList(processInstanceId);

        log.info("========================查询流程历史===成功=============================");
        return ReturnResult.success(historyList);
    }

    /**
     * 流程进度查询
     *
     * @param processInstanceId
     * @return
     */
    @GetMapping("/processStatus")
    public ReturnResult processStatus(@RequestParam("processInstanceId") String processInstanceId){
        log.info("========================流程进度查询===开始：参数:processInstanceId[{}]=============================", JSON.toJSONString(processInstanceId));

        if (StringUtils.isBlank(processInstanceId)){
            return ReturnResult.error("流程实例ID不能为空", ReturnMsg.ERROR1.getCode(), ReturnMsg.ERROR1.getMsg());
        }

        List<HistoricTaskInstance> list = processService.processStatus(processInstanceId);

        log.info("========================流程进度查询===成功=============================");
        return ReturnResult.success(list);
    }

    /**
     * 显示流程图
     *
     * @param processInstanceId
     * @param response
     */
    @GetMapping("/showDiagram")
    public void showDiagram(@RequestParam("processInstanceId") String processInstanceId, HttpServletResponse response){
        log.info("========================显示流程图===开始：参数:processInstanceId[{}]=============================", JSON.toJSONString(processInstanceId));

        OutputStream out = null;
        try {
            InputStream is = processService.getDiagramInputStream(processInstanceId);
            response.setContentType("multipart/form-data;charset=utf8");
            response.setHeader("Content-Type","image/png");
            out = response.getOutputStream();
            out.write(getImgByte(is));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (Objects.nonNull(out)) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        log.info("========================显示流程图===成功=============================");
    }

    /*============================================用户自定义流程===start============================================================================*/

    /**
     * 用户新增自定义流程
     *
     * @param param
     * @return
     */
    @PostMapping("/createProcessDef")
    @Transactional(rollbackFor = Exception.class)
    public ReturnResult createProcessDef(@RequestBody WorkFlowParam param){
        log.info("========================用户新增自定义流程===开始：参数:param[{}]=============================", JSON.toJSONString(param));
        //1. 流程定义保存到业务表中
        WorkFlow workFlow = param.getWorkFlow();
        workFlowService.save(workFlow);

        List<WorkFlowStep> stepList = param.getStepList();
        stepList = stepList.stream()
                .peek(workFlowStep -> workFlowStep.setWorkFlowId(workFlow.getId()))
                .sorted(Comparator.comparing(WorkFlowStep::getOrder))
                .collect(Collectors.toList());
        workFlowStepService.saveBatch(stepList);

        //2. 添加流程部署
        processService.addProcessDeployment(workFlow, stepList);

        log.info("========================用户新增自定义流程===结束=============================");
        return ReturnResult.success();
    }




    /*============================================用户自定义流程===end=============================================================================*/

    /*============================================以下为私有方法============================================================================*/

    /**
     * 将输入流转换为byte数组
     * @param is
     * @return
     * @throws IOException
     */
    private byte[] getImgByte(InputStream is) throws IOException {
        BufferedInputStream bufin = new BufferedInputStream(is);
        int buffSize = 1024;
        ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);

        byte[] temp = new byte[buffSize];
        int size = 0;
        while ((size = bufin.read(temp)) != -1) {
            out.write(temp, 0, size);
        }
        bufin.close();
        is.close();
        byte[] content = out.toByteArray();
        out.close();
        return content;
    }
}
