package ykk.cb.com.zcws.bean;

import java.io.Serializable;

import ykk.cb.com.zcws.bean.k3Bean.ICItem;

/**
 * 物料位置移动
 * @author Administrator
 *
 */
public class MaterialPositionMove implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private int mtlId;						// 物料id
	private int barcodeTableId;				// 条码表id
	private String barcode;					// 条码号
	private int oldStockId;					// 旧仓库id
	private int oldStockPositionId;			// 旧库位id
	private double oldInventoryQty;			// 旧仓库的即时库存
	private int newStockId;					// 新仓库id
	private int newStockPositionId;			// 新库位id
	private double newInventoryQty;			// 新仓库的仓库即时库存
	private double fqty;					// 移动数量
	private String k3Number;				// 对应金蝶的单号
	private int createUserId;				// 创建用户id
	private String createUserName;			// 创建用户名称
	private String createDate;				// 创建日期

	private ICItem material;
	private Stock oldStock;
	private StockPosition oldStockPosition;
	private Stock newStock;
	private StockPosition newStockPosition;

	// 临时字段，不存表
	private boolean check;			// 是否选中
	private String unitName;		// 单位名称

	public MaterialPositionMove() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMtlId() {
		return mtlId;
	}

	public void setMtlId(int mtlId) {
		this.mtlId = mtlId;
	}

	public int getBarcodeTableId() {
		return barcodeTableId;
	}

	public void setBarcodeTableId(int barcodeTableId) {
		this.barcodeTableId = barcodeTableId;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public int getOldStockId() {
		return oldStockId;
	}

	public void setOldStockId(int oldStockId) {
		this.oldStockId = oldStockId;
	}

	public int getOldStockPositionId() {
		return oldStockPositionId;
	}

	public void setOldStockPositionId(int oldStockPositionId) {
		this.oldStockPositionId = oldStockPositionId;
	}

	public int getNewStockId() {
		return newStockId;
	}

	public void setNewStockId(int newStockId) {
		this.newStockId = newStockId;
	}

	public int getNewStockPositionId() {
		return newStockPositionId;
	}

	public void setNewStockPositionId(int newStockPositionId) {
		this.newStockPositionId = newStockPositionId;
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

	public ICItem getMaterial() {
		return material;
	}

	public void setMaterial(ICItem material) {
		this.material = material;
	}
	
	public Stock getOldStock() {
		return oldStock;
	}

	public void setOldStock(Stock oldStock) {
		this.oldStock = oldStock;
	}

	public StockPosition getOldStockPosition() {
		return oldStockPosition;
	}

	public void setOldStockPosition(StockPosition oldStockPosition) {
		this.oldStockPosition = oldStockPosition;
	}

	public Stock getNewStock() {
		return newStock;
	}

	public void setNewStock(Stock newStock) {
		this.newStock = newStock;
	}

	public StockPosition getNewStockPosition() {
		return newStockPosition;
	}

	public void setNewStockPosition(StockPosition newStockPosition) {
		this.newStockPosition = newStockPosition;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public double getOldInventoryQty() {
		return oldInventoryQty;
	}

	public void setOldInventoryQty(double oldInventoryQty) {
		this.oldInventoryQty = oldInventoryQty;
	}

	public double getNewInventoryQty() {
		return newInventoryQty;
	}

	public void setNewInventoryQty(double newInventoryQty) {
		this.newInventoryQty = newInventoryQty;
	}

	public String getK3Number() {
		return k3Number;
	}

	public void setK3Number(String k3Number) {
		this.k3Number = k3Number;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}
	
}
