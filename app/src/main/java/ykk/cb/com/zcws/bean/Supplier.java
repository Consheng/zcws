package ykk.cb.com.zcws.bean;

import java.io.Serializable;

/**
 * @Description:供应商
 *
 * @author qxp 2019年3月7日 上午11:44:25
 */
public class Supplier implements Serializable {
	/* id */
	private int id;
	/* 供应商K3id */
	private int supplierId;
	/* 供应商编码 */
	private String fnumber;
	/* 供应商名称 */
	private String fname;
	/* 供应商来源账套Id */
	private int sourceAcctId;
	/* 供应商来源账套名称 */
	private String sourceAcctName;
	/* 供应商对应账套id */
	private int correspondId;
	/* 供应商对应账套名称 */
	private String correspondName;

	/* 创建时间 */
	private String createDate;
	/* 修改时间 */
	private String fModifyDate;

	public Supplier() {
		super();
	}

	public int getId() {
		return id;
	}

	public int getSupplierId() {
		return supplierId;
	}

	public String getFnumber() {
		return fnumber;
	}

	public String getFname() {
		return fname;
	}

	public int getSourceAcctId() {
		return sourceAcctId;
	}

	public String getSourceAcctName() {
		return sourceAcctName;
	}

	public int getCorrespondId() {
		return correspondId;
	}

	public String getCorrespondName() {
		return correspondName;
	}

	public String getCreateDate() {
		return createDate;
	}

	public String getfModifyDate() {
		return fModifyDate;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setSupplierId(int supplierId) {
		this.supplierId = supplierId;
	}

	public void setFnumber(String fnumber) {
		this.fnumber = fnumber;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public void setSourceAcctId(int sourceAcctId) {
		this.sourceAcctId = sourceAcctId;
	}

	public void setSourceAcctName(String sourceAcctName) {
		this.sourceAcctName = sourceAcctName;
	}

	public void setCorrespondId(int correspondId) {
		this.correspondId = correspondId;
	}

	public void setCorrespondName(String correspondName) {
		this.correspondName = correspondName;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public void setfModifyDate(String fModifyDate) {
		this.fModifyDate = fModifyDate;
	}


}
