package ykk.cb.com.zcws.bean.k3Bean;

import java.io.Serializable;

/**
 * 盘点方案表
 */
public class ICStockCheckProcess implements Serializable {
	private int fid; // 方案id
	private String fprocessId; // 方案名称
	
	public ICStockCheckProcess() {
		super();
	}
	
	public int getFid() {
		return fid;
	}
	public void setFid(int fid) {
		this.fid = fid;
	}
	public String getFprocessId() {
		return fprocessId;
	}
	public void setFprocessId(String fprocessId) {
		this.fprocessId = fprocessId;
	}
	
}
