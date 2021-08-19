package ykk.cb.com.zcws.bean.k3Bean;

import java.io.Serializable;

import ykk.cb.com.zcws.bean.Stock;
import ykk.cb.com.zcws.bean.StockPosition;
import ykk.cb.com.zcws.bean.User;

/**
 * 盘点备份表
 */
public class ICInvBackup implements Serializable {
	private int id; 					// id
	private int finterId; 				// 方案id
	private String accountType; 		// 账号类型（ZH：综合账号，DS：电商账号，XS：线束账号）
	private int stockId; 				// 仓库id
	private int stockPosId; 			// K3  库位id
	private int fitemId; 				// 物料id
	private String fbatchNo; 			// 物料批次
	private double fauxQty; 			// 账存数
	private double fauxQtyAct; 			// 实存数
	private double fauxCheckQty; 		// 盘点数
	private double realQty; 			// 当时盘点的输入的数
	private String createDate; 			// 创建日期
	private int createUserId; 			// 创建人
	private int toK3; 					// 是否提交到K3  1: 未提交	3:已提交
	private int repeatStatus;			// 复盘状态（ 0：未复盘，1：已复盘 ）
	private double repeatQty; 			// 复盘数
	private int repeatUserId; 			// 复盘人
	private String repeatDate; 			// 复盘时间

	private Stock stock;
	private StockPosition stockPos;
	private ICItem icItem;
	private User user;

	// 临时字段，不存表
	private boolean check; // 是否选中

	public ICInvBackup() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFinterId() {
		return finterId;
	}

	public void setFinterId(int finterId) {
		this.finterId = finterId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public int getStockId() {
		return stockId;
	}

	public void setStockId(int stockId) {
		this.stockId = stockId;
	}

	public int getStockPosId() {
		return stockPosId;
	}

	public void setStockPosId(int stockPosId) {
		this.stockPosId = stockPosId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public String getFbatchNo() {
		return fbatchNo;
	}

	public void setFbatchNo(String fbatchNo) {
		this.fbatchNo = fbatchNo;
	}

	public double getFauxQty() {
		return fauxQty;
	}

	public void setFauxQty(double fauxQty) {
		this.fauxQty = fauxQty;
	}

	public double getFauxQtyAct() {
		return fauxQtyAct;
	}

	public void setFauxQtyAct(double fauxQtyAct) {
		this.fauxQtyAct = fauxQtyAct;
	}

	public double getFauxCheckQty() {
		return fauxCheckQty;
	}

	public void setFauxCheckQty(double fauxCheckQty) {
		this.fauxCheckQty = fauxCheckQty;
	}

	public double getRealQty() {
		return realQty;
	}

	public void setRealQty(double realQty) {
		this.realQty = realQty;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public int getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}

	public int getToK3() {
		return toK3;
	}

	public void setToK3(int toK3) {
		this.toK3 = toK3;
	}

	public int getRepeatStatus() {
		return repeatStatus;
	}

	public void setRepeatStatus(int repeatStatus) {
		this.repeatStatus = repeatStatus;
	}

	public double getRepeatQty() {
		return repeatQty;
	}

	public void setRepeatQty(double repeatQty) {
		this.repeatQty = repeatQty;
	}

	public int getRepeatUserId() {
		return repeatUserId;
	}

	public void setRepeatUserId(int repeatUserId) {
		this.repeatUserId = repeatUserId;
	}

	public String getRepeatDate() {
		return repeatDate;
	}

	public void setRepeatDate(String repeatDate) {
		this.repeatDate = repeatDate;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public StockPosition getStockPos() {
		return stockPos;
	}

	public void setStockPos(StockPosition stockPos) {
		this.stockPos = stockPos;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}



}
