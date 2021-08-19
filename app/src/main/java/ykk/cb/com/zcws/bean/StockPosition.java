package ykk.cb.com.zcws.bean;

import java.io.Serializable;

/**
 * 库位表stock_position
 */
public class StockPosition implements Serializable {
    //库位Id
    private int fspId;
    //仓位编码
    private String fnumber;
    //仓位名称
    private String fname;
    //仓位详细名称
    private String ffullName;
    // 仓位组Id
    private int fspGroupId;

    private Stock stock;

    // 临时字段，不存表
    private String className; // 前段用到的，请勿删除

    public StockPosition() {
        super();
    }


    public int getFspId() {
        return fspId;
    }


    public String getFnumber() {
        return fnumber;
    }


    public String getFname() {
        return fname;
    }


    public String getFfullName() {
        return ffullName;
    }


    public int getFspGroupId() {
        return fspGroupId;
    }


    public void setFspId(int fspId) {
        this.fspId = fspId;
    }


    public void setFnumber(String fnumber) {
        this.fnumber = fnumber;
    }


    public void setFname(String fname) {
        this.fname = fname;
    }


    public void setFfullName(String ffullName) {
        this.ffullName = ffullName;
    }


    public void setFspGroupId(int fspGroupId) {
        this.fspGroupId = fspGroupId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Stock getStock() {
        return stock;
    }


    public void setStock(Stock stock) {
        this.stock = stock;
    }

}
