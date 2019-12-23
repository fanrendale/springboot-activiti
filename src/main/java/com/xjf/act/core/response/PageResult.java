package com.xjf.act.core.response;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {
    /**
     * 总记录数
     */
    private long total;
    /**
     * 当前页结果
     */
    private List<T> rows;

    public PageResult() {
    }

    public PageResult(long total, List<T> rows) {
        super();
        this.total = total;
        this.rows = rows;
    }

    public PageResult(IPage<T> iPage){
        super();
        this.total = iPage.getTotal();
        this.rows = iPage.getRecords();
    }


    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
