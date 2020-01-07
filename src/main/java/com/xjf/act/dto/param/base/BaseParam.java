package com.xjf.act.dto.param.base;

import lombok.Data;

import java.util.Objects;

/**
 * 参数的基本类，包含基本的参数
 *
 * @Author: XJF
 * @Date: 2019/12/3 15:08
 */
@Data
public class BaseParam {

    /**
     * 分页起始页
     */
    private Integer pageNum = 1;

    /**
     * 分页容量
     */
    private Integer pageSize = 10;

    public void setPageNum(Integer pageNum) {
        if (Objects.isNull(pageNum)){
            this.pageNum = 1;
        }else {
            this.pageNum = pageNum;
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
