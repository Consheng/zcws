package ykk.cb.com.zcws.bean.k3Bean;

/**
 * @Description:发货通知单查询状态字段
 *
 * @author 2019年5月14日 上午11:40:30
 */
public class SeoutStockSearchStatus {

	private String fbillNo;		// 发货通知单
	private String fdate;
	private double fqty;
	private double stockQty;	// 已发数
	private double unStockQty;	// 未发数

	public SeoutStockSearchStatus() {
		super();
	}

	public String getFbillNo() {
		return fbillNo;
	}

	public void setFbillNo(String fbillNo) {
		this.fbillNo = fbillNo;
	}

	public String getFdate() {
		return fdate;
	}

	public void setFdate(String fdate) {
		this.fdate = fdate;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getStockQty() {
		return stockQty;
	}

	public void setStockQty(double stockQty) {
		this.stockQty = stockQty;
	}

	public double getUnStockQty() {
		return unStockQty;
	}

	public void setUnStockQty(double unStockQty) {
		this.unStockQty = unStockQty;
	}

	
}
