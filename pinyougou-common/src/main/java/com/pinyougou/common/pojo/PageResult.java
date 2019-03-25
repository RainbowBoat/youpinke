package com.pinyougou.common.pojo;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    private long total;

    private List<?> list;

    public PageResult() {
    }

    public PageResult(long total, List<?> rows) {
        this.total = total;
        this.list = rows;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<?> getRows() {
        return list;
    }

    public void setRows(List<?> rows) {
        this.list = rows;
    }
}
