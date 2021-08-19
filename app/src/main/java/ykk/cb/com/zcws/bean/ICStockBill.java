package ykk.cb.com.zcws.bean;

import java.io.Serializable;

import ykk.cb.com.zcws.comm.Comm;

/**
 * Wms 本地的出入库	主表
 * @author Administrator
 *
 */
public class ICStockBill implements Serializable {
	private int id;
	private String pdaNo;				// 本地生产的流水号
	/* 单据类型( ZH_DBD:综合账号调拨单 )
	 */
	private String billType;
	private String wmsBillType;
	private char billStatus;			// 单据业务状态 (A：创建，B：审核，C：关闭)
	private String fdate;				// 操作日期
	private int ftranType;				// 单据类型
	private int frob;					// 红蓝字(1:蓝字，-1红字)
	private int fsupplyId;				// 供应商id
	private int fdeptId;				// 部门id
	private int fcustId;				// 客户id
	private int fempId;					// 业务员id
	private int fsmanagerId;			// 保管人id
	private int fmanagerId;				// 负责人id
	private int ffmanagerId;			// 验收人id
	private int fbillerId;				// 制单人id
	private int fselTranType;			// 源单类型

	private String yewuMan;				// 业务员
	private String baoguanMan;			// 保管人
	private String fuzheMan;			// 负责人
	private String yanshouMan;			// 验收人
	private int createUserId;			// 创建人id
	private String createUserName;		// 创建人
	private String createDate;			// 创建日期
	private int isToK3;					// 是否提交到K3
	private String k3Number;			// k3返回的单号
	private int missionBillId;			// 任务单id
	private String expressNo;			// 快递单号
	private String expressCompany;		// 快递公司

	private Supplier supplier;			// 供应商对象
	private Department department;	// 部门对象
	private Customer cust;	// 客户对象
	private MissionBill missionBill;

	// 临时字段，不存表
	private boolean showButton; 		// 是否显示操作按钮
	private String suppName;
	private String deptName;
	private String missionBillto;
	private String departmentName;
	private String summary; 			// 主表摘要
	private String strSourceNo; // 对应的源单单号
	private String outStockName; // 调出仓库
	private int isCommit;	// 是否提交到仓管确认
	private String applyMan;			// 申请人

	public ICStockBill() {
		super();
	}


	public int getId() {
		return id;
	}

	public String getSuppName() {
		return suppName;
	}

	public void setSuppName(String suppName) {
		this.suppName = suppName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public void setId(int id) {
		this.id = id;
	}


	public String getPdaNo() {
		return pdaNo;
	}


	public void setPdaNo(String pdaNo) {
		this.pdaNo = pdaNo;
	}


	public String getBillType() {
		return billType;
	}


	public void setBillType(String billType) {
		this.billType = billType;
	}


	public String getWmsBillType() {
		return wmsBillType;
	}


	public void setWmsBillType(String wmsBillType) {
		this.wmsBillType = wmsBillType;
	}


	public char getBillStatus() {
		return billStatus;
	}


	public void setBillStatus(char billStatus) {
		this.billStatus = billStatus;
	}


	public String getFdate() {
		return fdate;
	}


	public void setFdate(String fdate) {
		this.fdate = fdate;
	}


	public int getFtranType() {
		return ftranType;
	}


	public void setFtranType(int ftranType) {
		this.ftranType = ftranType;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public int getFrob() {
		return frob;
	}


	public void setFrob(int frob) {
		this.frob = frob;
	}


	public int getFsupplyId() {
		return fsupplyId;
	}


	public void setFsupplyId(int fsupplyId) {
		this.fsupplyId = fsupplyId;
	}


	public int getFdeptId() {
		return fdeptId;
	}


	public void setFdeptId(int fdeptId) {
		this.fdeptId = fdeptId;
	}


	public int getFcustId() {
		return fcustId;
	}


	public void setFcustId(int fcustId) {
		this.fcustId = fcustId;
	}


	public int getFempId() {
		return fempId;
	}


	public void setFempId(int fempId) {
		this.fempId = fempId;
	}


	public int getFsmanagerId() {
		return fsmanagerId;
	}


	public void setFsmanagerId(int fsmanagerId) {
		this.fsmanagerId = fsmanagerId;
	}


	public int getFmanagerId() {
		return fmanagerId;
	}


	public void setFmanagerId(int fmanagerId) {
		this.fmanagerId = fmanagerId;
	}


	public int getFfmanagerId() {
		return ffmanagerId;
	}


	public void setFfmanagerId(int ffmanagerId) {
		this.ffmanagerId = ffmanagerId;
	}


	public int getFbillerId() {
		return fbillerId;
	}


	public void setFbillerId(int fbillerId) {
		this.fbillerId = fbillerId;
	}


	public int getFselTranType() {
		return fselTranType;
	}


	public void setFselTranType(int fselTranType) {
		this.fselTranType = fselTranType;
	}


	public String getYewuMan() {
		return yewuMan;
	}


	public void setYewuMan(String yewuMan) {
		this.yewuMan = yewuMan;
	}


	public String getBaoguanMan() {
		return baoguanMan;
	}


	public void setBaoguanMan(String baoguanMan) {
		this.baoguanMan = baoguanMan;
	}


	public String getFuzheMan() {
		return fuzheMan;
	}


	public void setFuzheMan(String fuzheMan) {
		this.fuzheMan = fuzheMan;
	}


	public String getYanshouMan() {
		return yanshouMan;
	}


	public void setYanshouMan(String yanshouMan) {
		this.yanshouMan = yanshouMan;
	}


	public int getCreateUserId() {
		return createUserId;
	}


	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}


	public String getCreateUserName() {
		return createUserName;
	}


	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}


	public String getCreateDate() {
		return createDate;
	}


	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}


	public int getIsToK3() {
		return isToK3;
	}


	public void setIsToK3(int isToK3) {
		this.isToK3 = isToK3;
	}


	public String getK3Number() {
		return k3Number;
	}


	public void setK3Number(String k3Number) {
		this.k3Number = k3Number;
	}

	public Supplier getSupplier() {
		return supplier;
	}


	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}


	public Department getDepartment() {
		return department;
	}


	public void setDepartment(Department department) {
		this.department = department;
	}


	public Customer getCust() {
		return cust;
	}


	public void setCust(Customer cust) {
		this.cust = cust;
	}


	public boolean isShowButton() {
		return showButton;
	}


	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}


	public String getSummary() {
		return summary;
	}


	public void setSummary(String summary) {
		this.summary = summary;
	}


	public String getExpressNo() {
		return expressNo;
	}


	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}


	public String getExpressCompany() {
		return expressCompany;
	}

	public String getMissionBillto() {
		return missionBillto;
	}

	public void setMissionBillto(String missionBillto) {
		this.missionBillto = missionBillto;
	}

	public void setExpressCompany(String expressCompany) {
		this.expressCompany = expressCompany;
	}

	public int getMissionBillId() {
		return missionBillId;
	}

	public void setMissionBillId(int missionBillId) {
		this.missionBillId = missionBillId;
	}

	public MissionBill getMissionBill() {
		return missionBill;
	}

	public void setMissionBill(MissionBill missionBill) {
		this.missionBill = missionBill;
	}

	public String getStrSourceNo() {
		// 存在大写的逗号（，）,且大于1
		if(Comm.isNULLS(strSourceNo).indexOf("，") > -1 && Comm.isNULLS(strSourceNo).length() > 0) {
			return strSourceNo.substring(0, strSourceNo.length()-1);
		}
		return strSourceNo;
	}

	public void setStrSourceNo(String strSourceNo) {
		this.strSourceNo = strSourceNo;
	}
	public String getOutStockName() {
		return outStockName;
	}
	public void setOutStockName(String outStockName) {
		this.outStockName = outStockName;
	}

	public int getIsCommit() {
		return isCommit;
	}

	public void setIsCommit(int isCommit) {
		this.isCommit = isCommit;
	}

	public String getApplyMan() {
		return applyMan;
	}


	public void setApplyMan(String applyMan) {
		this.applyMan = applyMan;
	}


}
