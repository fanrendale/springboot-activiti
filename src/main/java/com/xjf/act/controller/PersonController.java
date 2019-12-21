package com.xjf.act.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xjf.act.entity.Person;
import com.xjf.act.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xjf.act.controller.base.BaseController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xjf
 * @since 2019-12-21
 */
@RestController
@RequestMapping("/person")
@Slf4j
public class PersonController extends BaseController {

    @Autowired
    private PersonService personService;

    /**
     * 查询所有用户
     * @return
     */
    @GetMapping("/list")
    public Object list(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize){
        log.info("测试info级别日志");
        log.warn("测试warn级别日志");
        log.error("测试error级别日志");

        log.info("========================查询所有用户===开始：参数:param[pageNum:{}, pageSize:{}]=============================", JSON.toJSONString(pageNum), JSON.toJSONString(pageSize));
        Page<Person> page = new Page<>(pageNum, pageSize);
        IPage<Person> iPage = personService.page(page);

        log.info("========================查询所有用户===结束=============================");
        return iPage;
    }
}

