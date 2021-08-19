package ykk.cb.com.zcws.bean.k3Bean;

import java.io.Serializable;

import ykk.cb.com.zcws.bean.Stock;
import ykk.cb.com.zcws.bean.StockPosition;

/**
 * @Description:物料实体
 *
 * @author qxp 2019年2月26日 下午4:33:41
 */
public class ICItem implements Serializable{
	/* 物料id */
	private int fitemid;
	/* 物料规格型号 */
	private String fmodel;
	/* 物料名称 */
	private String fname;
	/* 是否禁用 */
	private Short fdeleted;
	/* 物料代码 */
	private String fnumber;
	/* 物料属性id */
	private int ferpclsid;
	/* 物料单位id */
	private int funitid;
	/* 物料数量精度 */
	private Short fqtydecimal;
	/* 物料最低库存量 */
	private double flowlimit;
	/* 物料最高库存量 */
	private double fhighlimit;
	/* 物料安全存量 */
	private double fsecinv;
	/* 物料分类id */
	private int ftypeid;

	/* 是否启用批次号管理F_105，990155=不启用，990156=启用(不是k3自带的，是另外增加的物料字段) */
	private int batchManager;

	/* 是否启用序列号管理F_106，990155=不启用，990156=启用(不是k3自带的，是另外增加的物料字段) */
	private int snManager;

	/* 是否赠品，990160=是 (不是k3自带的，是另外增加的) */
	private int isComplimentary;

	/* 旧物料编码 */
	private String oldItemNumber;
	/* 旧物料名称 */
	private String oldItemName;



	/* 适用车型  对应数据库F_104*/
	private String suitVehicleType;

	/*功能说明  对应数据库F_108*/
	private String functionDescription;

	private String fbarcode;

	/* 计量单位名称，不存库 */
	private String unitName;
	/* 计量单位代码，不存库 */
	private String unitNumber;
	private int stockId;	// 物料默认仓库
	private int stockPosId;	// 物料默认库位

	private Stock stock; // 仓库
	private StockPosition stockPos; // 库位

	// 临时字段，不加表
	private double inventoryQty; // 即时库存
	private String FHelpCode; //助记码
	private boolean check; // 是否选中
	private String smBatchCode; // 扫描的批次号
	private String smSnCode; // 扫描的序列号

	public ICItem() {
		super();
	}

	public int getFitemid() {
		return fitemid;
	}

	public void setFitemid(int fitemid) {
		this.fitemid = fitemid;
	}

	public String getFmodel() {
		return fmodel;
	}

	public void setFmodel(String fmodel) {
		this.fmodel = fmodel;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public Short getFdeleted() {
		return fdeleted;
	}

	public void setFdeleted(Short fdeleted) {
		this.fdeleted = fdeleted;
	}

	public String getFnumber() {
		return fnumber;
	}

	public void setFnumber(String fnumber) {
		this.fnumber = fnumber;
	}

	public int getFerpclsid() {
		return ferpclsid;
	}

	public void setFerpclsid(int ferpclsid) {
		this.ferpclsid = ferpclsid;
	}

	public int getFunitid() {
		return funitid;
	}

	public void setFunitid(int funitid) {
		this.funitid = funitid;
	}

	public Short getFqtydecimal() {
		return fqtydecimal;
	}

	public void setFqtydecimal(Short fqtydecimal) {
		this.fqtydecimal = fqtydecimal;
	}

	public double getFlowlimit() {
		return flowlimit;
	}

	public void setFlowlimit(double flowlimit) {
		this.flowlimit = flowlimit;
	}

	public double getFhighlimit() {
		return fhighlimit;
	}

	public void setFhighlimit(double fhighlimit) {
		this.fhighlimit = fhighlimit;
	}

	public double getFsecinv() {
		return fsecinv;
	}

	public void setFsecinv(double fsecinv) {
		this.fsecinv = fsecinv;
	}

	public int getFtypeid() {
		return ftypeid;
	}

	public void setFtypeid(int ftypeid) {
		this.ftypeid = ftypeid;
	}

	public int getBatchManager() {
		return batchManager;
	}

	public void setBatchManager(int batchManager) {
		this.batchManager = batchManager;
	}

	public int getSnManager() {
		return snManager;
	}

	public void setSnManager(int snManager) {
		this.snManager = snManager;
	}

	public String getOldItemNumber() {
		return oldItemNumber;
	}

	public void setOldItemNumber(String oldItemNumber) {
		this.oldItemNumber = oldItemNumber;
	}

	public String getOldItemName() {
		return oldItemName;
	}

	public void setOldItemName(String oldItemName) {
		this.oldItemName = oldItemName;
	}




	public String getSuitVehicleType() {
		return suitVehicleType;
	}

	public void setSuitVehicleType(String suitVehicleType) {
		this.suitVehicleType = suitVehicleType;
	}

	public String getFbarcode() {
		return fbarcode;
	}

	public void setFbarcode(String fbarcode) {
		this.fbarcode = fbarcode;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitNumber() {
		return unitNumber;
	}

	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}

	public int getIsComplimentary() {
		return isComplimentary;
	}

	public void setIsComplimentary(int isComplimentary) {
		this.isComplimentary = isComplimentary;
	}
	public String getFunctionDescription() {
		return functionDescription;
	}

	public void setFunctionDescription(String functionDescription) {
		this.functionDescription = functionDescription;
	}

	public Stock getStock() {
		return stock;
	}

	public StockPosition getStockPos() {
		return stockPos;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public void setStockPos(StockPosition stockPos) {
		this.stockPos = stockPos;
	}

	public double getInventoryQty() {
		return inventoryQty;
	}

	public void setInventoryQty(double inventoryQty) {
		this.inventoryQty = inventoryQty;
	}

	public String getFHelpCode() {
		return FHelpCode;
	}

	public void setFHelpCode(String FHelpCode) {
		this.FHelpCode = FHelpCode;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public String getSmBatchCode() {
		return smBatchCode;
	}

	public void setSmBatchCode(String smBatchCode) {
		this.smBatchCode = smBatchCode;
	}

	public String getSmSnCode() {
		return smSnCode;
	}

	public void setSmSnCode(String smSnCode) {
		this.smSnCode = smSnCode;
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

}