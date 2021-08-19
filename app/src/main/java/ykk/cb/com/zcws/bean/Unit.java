package ykk.cb.com.zcws.bean;

import java.io.Serializable;

public class Unit implements Serializable{
	/*单位内码*/
	private int funitId;
	/*分配内码*/
	private String fmasterid;
	/*单位编码*/
	private String unitNumber;
	/*单位名称*/
	private String unitName;
	/*K3数据状态*/
	private String dataStatus;
	/*wms非物理删除标识*/
	private String isDelete;
	/*k3是否禁用*/
	private String enabled;
	/*修改日期*/
	private String fmodifyDate;

	public Unit() {
		super();
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public String getFmasterid() {
		return fmasterid;
	}

	public void setFmasterid(String fmasterid) {
		this.fmasterid = fmasterid;
	}

	public String getUnitNumber() {
		return unitNumber;
	}

	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getDataStatus() {
		return dataStatus;
	}

	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}

	public String getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getFmodifyDate() {
		return fmodifyDate;
	}

	public void setFmodifyDate(String fmodifyDate) {
		this.fmodifyDate = fmodifyDate;
	}
	
}
