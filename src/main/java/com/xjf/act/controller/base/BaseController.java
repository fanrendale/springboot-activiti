package com.xjf.act.controller.base;

import java.util.Map;

/**
 * 公共父类
 *
 * @Author: xjf
 * @Since: 2019/12/21 11:48
 */
public class BaseController {

    /**
     * 获取当前登录用户的ID
     * @return
     */
    protected Long getCurrentUserId(){
        /*Long id = null;
        Subject subject = SecurityUtils.getSubject();
        Object principal =  subject.getPrincipal();
        if (principal instanceof SSOToken){
            SSOToken ssoToken = (SSOToken) principal;
            Map<String,Object> map = (Map<String, Object>) redisUtil.get(RedisUtil.ACCESS_TOKEN + ssoToken.getAccessToken());
            id = Long.parseLong(map.get("id").toString());
        }

        return id;*/
        return 1l;
    }
}
