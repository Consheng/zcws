package ykk.cb.com.zcws.bean;

import java.io.Serializable;

public class Staff implements Serializable {
	/*员工id*/
	private int id;
	/*k3员工id*/
	private int staffId;
	/*员工编号*/
	private String number;
	/*员工名称*/
	private String name;
	/*员工创建组织*/
	private int staffCreateOrgId;
	/*员工使用组织*/
	private int staffUseOrgId;
	/*组织实体类*/
	private Organization organization;
	/*员工工作组织*/
	private int staffWorkOrgId;
	/*部门id*/
	private int deptId;
	/*部门*/
	private Department department;
	/*岗位id*/
	private int postId;
	/*岗位*/
	/*K3数据状态*/
	private String dataStatus;
	/*wms非物理删除标识*/
	private String isDelete;
	/*k3是否禁用*/
	private String enabled;
	/*修改日期*/
	private String fModifyDate;

	// 临时字段
	private int isCheck;

	/**
	 * 构造方法
	 */
	public Staff() {
		super();
	}
	public int getId() {
		return id;
	}
	public int getStaffId() {
		return staffId;
	}
	public String getNumber() {
		return number;
	}
	public String getName() {
		return name;
	}
	public int getStaffCreateOrgId() {
		return staffCreateOrgId;
	}
	public int getStaffUseOrgId() {
		return staffUseOrgId;
	}
	public Organization getOrganization() {
		return organization;
	}
	public int getStaffWorkOrgId() {
		return staffWorkOrgId;
	}
	public int getDeptId() {
		return deptId;
	}
	public Department getDepartment() {
		return department;
	}
	public int getPostId() {
		return postId;
	}
	public String getDataStatus() {
		return dataStatus;
	}
	public String getIsDelete() {
		return isDelete;
	}
	public String getEnabled() {
		return enabled;
	}
	public String getfModifyDate() {
		return fModifyDate;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setStaffCreateOrgId(int staffCreateOrgId) {
		this.staffCreateOrgId = staffCreateOrgId;
	}
	public void setStaffUseOrgId(int staffUseOrgId) {
		this.staffUseOrgId = staffUseOrgId;
	}
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	public void setStaffWorkOrgId(int staffWorkOrgId) {
		this.staffWorkOrgId = staffWorkOrgId;
	}
	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	public void setPostId(int postId) {
		this.postId = postId;
	}
	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	public void setIsDelete(String isDelete) {
		this.isDelete = isDelete;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	public void setfModifyDate(String fModifyDate) {
		this.fModifyDate = fModifyDate;
	}

	public int getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(int isCheck) {
		this.isCheck = isCheck;
	}



}
