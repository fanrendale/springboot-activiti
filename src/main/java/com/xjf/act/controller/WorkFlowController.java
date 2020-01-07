package com.xjf.act.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xjf.act.controller.base.BaseController;
import com.xjf.act.core.exception.BusinessException;
import com.xjf.act.core.response.PageResult;
import com.xjf.act.core.response.ReturnMsg;
import com.xjf.act.core.response.ReturnResult;
import com.xjf.act.dto.param.WorkFlowQueryParam;
import com.xjf.act.entity.WorkFlow;
import com.xjf.act.entity.WorkFlowStep;
import com.xjf.act.service.WorkFlowService;
import com.xjf.act.service.WorkFlowStepService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 自定义流程描述表 前端控制器
 * </p>
 *
 * @author xjf
 * @since 2019-12-30
 */
@RestController
@RequestMapping("/workFlow")
@Slf4j
public class WorkFlowController extends BaseController {

    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkFlowStepService workFlowStepService;

    /**
     * 自定义流程条件查询
     * @param param
     * @return
     */
    @GetMapping("/conditionQuery")
    public ReturnResult conditionQuery(WorkFlowQueryParam param){
        log.info("========================自定义流程条件查询===开始：参数:param[{}]=============================", JSON.toJSONString(param));

        LambdaQueryWrapper<WorkFlow> workFlowWrapper = new LambdaQueryWrapper<>();
        workFlowWrapper.orderByDesc(WorkFlow::getCreateTime);
        if (StringUtils.isNotBlank(param.getName())){
            workFlowWrapper.like(WorkFlow::getName, param.getName());
        }

        Page<WorkFlow> page = new Page<>(param.getPageNum(), param.getPageSize());
        IPage<WorkFlow> iPage = workFlowService.page(page, workFlowWrapper);

        log.info("========================自定义流程条件查询===成功=============================");
        return ReturnResult.success(new PageResult<>(iPage.getTotal(),iPage.getRecords()));
    }

    /**
     * 根据自定义流程ID查询对应的步骤
     * @param workFlowId
     * @return
     */
    @GetMapping("/selectStepByWorkFlowId")
    public ReturnResult selectStepByWorkFlowId(@RequestParam("workFlowId") String workFlowId){
        log.info("========================根据自定义流程ID查询对应的步骤===开始：参数:workFlowId[{}]=============================", JSON.toJSONString(workFlowId));

        if (StringUtils.isBlank(workFlowId)){
            throw new BusinessException("自定义流程ID不能为空");
        }

        LambdaQueryWrapper<WorkFlowStep> stepWrapper = new LambdaQueryWrapper<>();
        stepWrapper.orderByAsc(WorkFlowStep::getOrder);
        stepWrapper.eq(WorkFlowStep::getWorkFlowId, workFlowId);

        List<WorkFlowStep> list = workFlowStepService.list(stepWrapper);

        log.info("========================根据自定义流程ID查询对应的步骤===成功=============================");
        return ReturnResult.success(list);
    }

    /**
     * 自定义流程批量删除
     *
     * @param param
     * @return
     */
    @PostMapping("/batchDel")
    public ReturnResult batchDel(@RequestBody Long[] param){
        log.info("========================自定义流程批量删除===开始：参数:param[ids:{}]=============================", JSON.toJSONString(param));

        if (Objects.nonNull(param) && param.length > 0) {
            boolean flag = workFlowService.removeByIds(Arrays.asList(param));
            //同时删除流程对应的步骤信息
            // TODO: 2020/1/2 流程部署信息没有删除
            LambdaQueryWrapper<WorkFlowStep> stepWrapper = new LambdaQueryWrapper<>();
            stepWrapper.in(WorkFlowStep::getWorkFlowId,Arrays.asList(param));
            workFlowStepService.remove(stepWrapper);

            if (flag){
                log.info("========================自定义流程批量删除===成功=============================");
                return ReturnResult.success("批量删除成功");
            }
        }

        log.error("========================自定义流程批量删除===失败=============================");
        return ReturnResult.error(ReturnMsg.EXCEPTION2.getCode(),"批量删除失败");
    }
}

