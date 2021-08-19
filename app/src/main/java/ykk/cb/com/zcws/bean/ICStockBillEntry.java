package ykk.cb.com.zcws.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ykk.cb.com.zcws.bean.k3Bean.ICItem;
import ykk.cb.com.zcws.comm.Comm;

/**
 * Wms 本地的出入库	Entry表
 * @author Administrator
 *
 */
public class ICStockBillEntry implements Serializable {
	private int id; 					//
	private int icstockBillId;			// 主表id
	private int finterId;				// 主表id
	private int fitemId;				// 物料id
	private int fentryId;				// 分录id
	private int fdcStockId;				// 调入仓库id
	private int fdcSPId;				// 调入库位id
	private int fscStockId;				// 调出仓库id
	private int fscSPId;				// 调出库位id
	private double fqty;				// 数量
	private double fprice;				// 单价
	private double ftaxRate;			// 税率
	private double ftaxPrice;			// 含税单价
	private int funitId;				// 单位id
	private int fsourceInterId;			// 来源内码id
	private int fsourceEntryId;			// 来源分录id
	private int fsourceTranType;		// 来源类型
	private String fsourceBillNo;		// 来源单号
	private double fsourceQty;			// 来源单数量
	private int forderInterId;			// 来源订单id
	private String forderBillNo;		// 来源订单号
	private int forderEntryId;			// 来源订单分录id
	private int fdetailId; 				// 来源分类唯一行标识
	private double qcPassQty;			// 质检合格数
	private String fkfDate; 			// 生产/采购日期
	private int fkfPeriod;				// 保质期
	private int sourceThisId;			// 来源本身id
	private String remark;				// 备注

	private ICStockBill icstockBill;
	private Stock stock;
	private StockPosition stockPos;
	private Stock stock2;
	private StockPosition stockPos2;
	private ICItem icItem;
	private Unit unit;

	// 临时字段，不存表
	private boolean showButton; 		// 是否显示操作按钮
	private String smBatchCode; // 扫码的批次号
	private String smSnCode; // 扫码的序列号
	private double smQty;	// 扫码后计算出的数
	private String strBatchCode; // 拼接的批次号
	private String strBarcode;	// 用于显示拼接的条码号
	private double inventoryNowQty; // 当前扫码的可用库存数

	private ICStockBillEntry sourceThis; // 来源本身对象
	private List<ICStockBillEntry_Barcode> icstockBillEntry_Barcodes; // 条码记录

	public ICStockBillEntry() {
		super();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIcstockBillId() {
		return icstockBillId;
	}

	public void setIcstockBillId(int icstockBillId) {
		this.icstockBillId = icstockBillId;
	}

	public int getFinterId() {
		return finterId;
	}

	public void setFinterId(int finterId) {
		this.finterId = finterId;
	}

	public int getFitemId() {
		return fitemId;
	}

	public void setFitemId(int fitemId) {
		this.fitemId = fitemId;
	}

	public int getFentryId() {
		return fentryId;
	}

	public void setFentryId(int fentryId) {
		this.fentryId = fentryId;
	}

	public int getFdcStockId() {
		return fdcStockId;
	}

	public void setFdcStockId(int fdcStockId) {
		this.fdcStockId = fdcStockId;
	}

	public int getFdcSPId() {
		return fdcSPId;
	}

	public void setFdcSPId(int fdcSPId) {
		this.fdcSPId = fdcSPId;
	}

	public int getFscStockId() {
		return fscStockId;
	}

	public void setFscStockId(int fscStockId) {
		this.fscStockId = fscStockId;
	}

	public int getFscSPId() {
		return fscSPId;
	}

	public void setFscSPId(int fscSPId) {
		this.fscSPId = fscSPId;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFprice() {
		return fprice;
	}

	public void setFprice(double fprice) {
		this.fprice = fprice;
	}

	public double getFtaxRate() {
		return ftaxRate;
	}

	public void setFtaxRate(double ftaxRate) {
		this.ftaxRate = ftaxRate;
	}

	public double getFtaxPrice() {
		return ftaxPrice;
	}

	public void setFtaxPrice(double ftaxPrice) {
		this.ftaxPrice = ftaxPrice;
	}

	public int getFunitId() {
		return funitId;
	}

	public void setFunitId(int funitId) {
		this.funitId = funitId;
	}

	public int getFsourceInterId() {
		return fsourceInterId;
	}

	public void setFsourceInterId(int fsourceInterId) {
		this.fsourceInterId = fsourceInterId;
	}

	public int getFsourceEntryId() {
		return fsourceEntryId;
	}

	public void setFsourceEntryId(int fsourceEntryId) {
		this.fsourceEntryId = fsourceEntryId;
	}

	public int getFsourceTranType() {
		return fsourceTranType;
	}

	public void setFsourceTranType(int fsourceTranType) {
		this.fsourceTranType = fsourceTranType;
	}

	public String getFsourceBillNo() {
		return fsourceBillNo;
	}

	public void setFsourceBillNo(String fsourceBillNo) {
		this.fsourceBillNo = fsourceBillNo;
	}

	public double getFsourceQty() {
		return fsourceQty;
	}

	public void setFsourceQty(double fsourceQty) {
		this.fsourceQty = fsourceQty;
	}

	public int getForderInterId() {
		return forderInterId;
	}

	public void setForderInterId(int forderInterId) {
		this.forderInterId = forderInterId;
	}

	public String getForderBillNo() {
		return forderBillNo;
	}

	public void setForderBillNo(String forderBillNo) {
		this.forderBillNo = forderBillNo;
	}

	public int getForderEntryId() {
		return forderEntryId;
	}

	public void setForderEntryId(int forderEntryId) {
		this.forderEntryId = forderEntryId;
	}

	public int getFdetailId() {
		return fdetailId;
	}

	public void setFdetailId(int fdetailId) {
		this.fdetailId = fdetailId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public double getQcPassQty() {
		return qcPassQty;
	}

	public void setQcPassQty(double qcPassQty) {
		this.qcPassQty = qcPassQty;
	}

	public String getFkfDate() {
		return fkfDate;
	}

	public void setFkfDate(String fkfDate) {
		this.fkfDate = fkfDate;
	}

	public int getFkfPeriod() {
		return fkfPeriod;
	}

	public void setFkfPeriod(int fkfPeriod) {
		this.fkfPeriod = fkfPeriod;
	}

	public int getSourceThisId() {
		return sourceThisId;
	}

	public void setSourceThisId(int sourceThisId) {
		this.sourceThisId = sourceThisId;
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

	public Stock getStock2() {
		return stock2;
	}

	public void setStock2(Stock stock2) {
		this.stock2 = stock2;
	}

	public StockPosition getStockPos2() {
		return stockPos2;
	}

	public void setStockPos2(StockPosition stockPos2) {
		this.stockPos2 = stockPos2;
	}

	public ICItem getIcItem() {
		return icItem;
	}

	public void setIcItem(ICItem icItem) {
		this.icItem = icItem;
	}

	public ICStockBill getIcstockBill() {
		return icstockBill;
	}

	public void setIcstockBill(ICStockBill icstockBill) {
		this.icstockBill = icstockBill;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
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

	public String getStrBatchCode() {
		// 存在大写的逗号（，）,且大于1
		if(Comm.isNULLS(strBatchCode).indexOf("，") > -1 && Comm.isNULLS(strBatchCode).length() > 0) {
			return strBatchCode.substring(0, strBatchCode.length()-1);
		}
		return strBatchCode;
	}

	public void setStrBatchCode(String strBatchCode) {
		this.strBatchCode = strBatchCode;
	}

	public String getStrBarcode() {
		return strBarcode;
	}

	public void setStrBarcode(String strBarcode) {
		this.strBarcode = strBarcode;
	}

	public ICStockBillEntry getSourceThis() {
		return sourceThis;
	}

	public void setSourceThis(ICStockBillEntry sourceThis) {
		this.sourceThis = sourceThis;
	}

	public List<ICStockBillEntry_Barcode> getIcstockBillEntry_Barcodes() {
		if(icstockBillEntry_Barcodes == null) {
			icstockBillEntry_Barcodes = new ArrayList<>();
		}
		return icstockBillEntry_Barcodes;
	}

	public void setIcstockBillEntry_Barcodes(List<ICStockBillEntry_Barcode> icstockBillEntry_Barcodes) {
		this.icstockBillEntry_Barcodes = icstockBillEntry_Barcodes;
	}

	public double getSmQty() {
		return smQty;
	}

	public void setSmQty(double smQty) {
		this.smQty = smQty;
	}

	public double getInventoryNowQty() {
		return inventoryNowQty;
	}

	public void setInventoryNowQty(double inventoryNowQty) {
		this.inventoryNowQty = inventoryNowQty;
	}



}
