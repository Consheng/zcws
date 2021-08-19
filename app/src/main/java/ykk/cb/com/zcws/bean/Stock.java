package ykk.cb.com.zcws.bean;

import java.io.Serializable;

/**
 * 仓库表 t_stock
 */
public class Stock implements Serializable {
    private int fitemId;

    private String fname;

    private String fnumber;

    //仓位组id
    private int fspGroupId;

    //是否进行仓位组管理 1：开启仓位管理   0：不开启仓位管理
    private int fisStockMgr;

    // 临时字段，不存表
    private String className; // 前段用到的，请勿删除

    public Stock() {
        super();
    }

    public int getFitemId() {
        return fitemId;
    }

    public String getFname() {
        return fname;
    }

    public String getFnumber() {
        return fnumber;
    }

    public int getFspGroupId() {
        return fspGroupId;
    }

    public int getFisStockMgr() {
        return fisStockMgr;
    }

    public void setFitemId(int fitemId) {
        this.fitemId = fitemId;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setFnumber(String fnumber) {
        this.fnumber = fnumber;
    }

    public void setFspGroupId(int fspGroupId) {
        this.fspGroupId = fspGroupId;
    }

    public void setFisStockMgr(int fisStockMgr) {
        this.fisStockMgr = fisStockMgr;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
