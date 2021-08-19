package ykk.cb.com.zcws.bean;

import java.io.Serializable;

/**
 * 调拨申请单主表
 * @author Administrator
 *
 */
public class TransferApply implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String billNo;			// 唯一单号
	private int stockId;			//
	private int status;				// 状态，1：创建，2，部分拣货，3，拣货完成
	private int createUserId;		// 创建人id
	private String createDate;		// 创建日期

	private Stock stock;			// 对应的仓库
	private User createUser;		// 对应的创建用户对象

	// 临时对象，不存表
	private int mtlCount;			// 物料个数

	public TransferApply() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBillNo() {
		return billNo;
	}

	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public User getCreateUser() {
		return createUser;
	}

	public void setCreateUser(User createUser) {
		this.createUser = createUser;
	}

	public int getMtlCount() {
		return mtlCount;
	}

	public void setMtlCount(int mtlCount) {
		this.mtlCount = mtlCount;
	}


	
}
