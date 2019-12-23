package com.xjf.act.util;

import org.springframework.beans.BeanUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 集合List的工具类
 *
 * @Author: XJF
 * @Date: 2019/12/2 20:06
 */
public class ListUtil<T> {

    /**
     * List跟List的相同属性的复制，需要每次使用进行实例化
     *
     * @param obj 源数据
     * @param list2 目标list
     * @param classObj 目标list的实体class
     */
    public void copyList(Object obj, List<T> list2, Class<T> classObj) {
        if ((!Objects.isNull(obj)) && (!Objects.isNull(list2))) {
            List list1 = (List) obj;
            list1.forEach(item -> {
                try {
                    T data = classObj.newInstance();
                    BeanUtils.copyProperties(item, data);
                    list2.add(data);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }


            });
        }
    }

    /**
     * 集合判断是否为空
     *
     * @param collection
     * @return
     */
    public static boolean notEmpty(Collection collection) {
        if (collection != null) {
            for (Object next : collection) {
                if (next != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
