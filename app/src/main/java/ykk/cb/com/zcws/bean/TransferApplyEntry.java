package ykk.cb.com.zcws.bean;

import java.io.Serializable;

import ykk.cb.com.zcws.bean.k3Bean.ICItem;

/**
 * 调拨申请单分录表
 * @author Administrator
 *
 */
public class TransferApplyEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int parentId;			// 主表id
	private int icitemId;			// 物料id
	private double fqty;			// 申请数量
	private double commitQty;		// 提交数量
	private int stockId;			// 所在仓库
	private int stockPosId;			// 所在库位

	private TransferApply transferApply; // 主表对象
	private ICItem icItem;			// 物料对象
	private Stock stock;			// 仓库对象
	private StockPosition stockPos;	// 库位对象

	// 临时字段，不加表
	private double useableQty; // 可用数

	public TransferApplyEntry() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getIcitemId() {
		return icitemId;
	}

	public void setIcitemId(int icitemId) {
		this.icitemId = icitemId;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getCommitQty() {
		return commitQty;
	}

	public void setCommitQty(double commitQty) {
		this.commitQty = commitQty;
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

	public TransferApply getTransferApply() {
		return transferApply;
	}

	public void setTransferApply(TransferApply transferApply) {
		this.transferApply = transferApply;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
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

	public double getUseableQty() {
		return useableQty;
	}

	public void setUseableQty(double useableQty) {
		this.useableQty = useableQty;
	}
	
	
}
