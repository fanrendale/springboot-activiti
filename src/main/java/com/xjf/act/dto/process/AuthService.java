package com.xjf.act.dto.process;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 流程bpmn图定义中的JUEL表达式变量可以获取的参数方法
 *
 * @Author: xjf
 * @Since: 2019/12/18 16:12
 */
@Component
public class AuthService implements Serializable {

    /*@Autowired
    private GroupService groupService;

    *//**
     * 获取所有小组的组长id集合
     *
     * @return
     *//*
    public List<String> getGroupLeaderList(){
        List<Group> list = groupService.list();

        return list.stream().map(group -> String.valueOf(group.getLeaderId())).collect(Collectors.toList());
    }*/
}
