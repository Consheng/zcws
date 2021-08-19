package ykk.cb.com.zcws.bean.k3Bean;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description:订单详细实体
 *
 * @author qxp 2019年2月26日 下午4:34:06
 */
public class SeOrderEntry implements Serializable {
	private int fdetailid;/**/

	private String fbrno;/* 公司机构内码 */

	private int finterid;/* 订单内码 */

	private int fentryid;/* 分录号 */

	private int fitemid;/* 产品代码 */

	private double fqty;/* 基本单位数量 */

	private double fcommitqty;/* 发货数量 */

	private double fprice;/* 单价 */

	private double famount;/* 金额 */

	private double ftaxrate;/* 折扣率(%) */

	private double ftaxamount;/* 折扣额 */

	private double ftax;/* 税金（本位币） */

	private Float fdiscount;/* 折扣 */

	private String fnote;/* 备注 */

	private Date fdate;/* 交货日期 */

	private double fdiscountamount;/* 折扣金额 */

	private double finvoiceqty;/* 发票数量 */

	private double fbcommitqty;/* 退货数量 */

	private int ftranleadtime;/* 运输提前期 */

	private int fatpdeduct;/* 冲减标志 */

	private int fcostobjectid;/* 成本对象代码 */

	private int funitid;/* 单位 */

	private double fauxbcommitqty;/* 辅助退货数量 */

	private double fauxcommitqty;/* 辅助发货数量 */

	private double fauxinvoiceqty;/* 辅助开票数量 */

	private double fauxprice;/* 实际含税单价 */

	private double fauxqty;/* 数量 */

	private double funidiscount;/* 单位折扣额 */

	private double ffinalamount;/* 折后金额 */

	private int fsourceentryid;/* 源单行号 */

	private int fhavemrp;/* 是否参加过MRP计算 */

	private double fstockqty;/* 出库数量 */

	private double fauxstockqty;/* 出库数量(辅助单位): */

	private String fbatchno;/* 物料批号 */

	private double fcess;/* 税率(%) */

	private Date fadviceconsigndate;/* 建议交货日期 */

	private int fbominterid;/* 批号/客户BOM */

	private String fmapnumber;/* 对应代码 */

	private String fmapname;/* 对应名称 */

	private int flockflag;/* 锁库标志 */

	private int finforecast;/* 是否预测内 */

	private double fallstdamount;/* 价税合计(本位币) */

	private int fauxpropid;/* 辅助属性 */

	private double fauxpricediscount;/* 实际含税单价 */

	private double ftaxamt;/* 销项税额 */

	private int fsourcetrantype;/* 源单类型 */

	private int fsourceinterid;/* 源单内码 */

	private String fsourcebillno;/* 源单单号 */

	private int fcontractinterid;/* 合同内码 */

	private int fcontractentryid;/* 合同分录 */

	private String fcontractbillno;/* 合同单号 */

	private double fcommitinstall;/* 基本单位组装数量 */

	private double fauxcommitinstall;/* 组装数量 */

	private double fseccommitinstall;/* 辅助单位组装数量 */

	private int fplanmode;/* 计划模式 */

	private String fmtono;/* 计划跟踪号 */

	private String forderbillno;/* 客户订单号 */

	private int forderentryid;/* 订单行号 */

	private int fbomcategory;/* BOM类別 */

	private int forderbomstatus;/* 订单BOM状态 */

	private int forderbominterid;/* 订单BOM内码 */

	private int foutsourceinterid;/* 发送内码 */

	private int foutsourceentryid;/* 发送分录 */

	private int foutsourcetrantype;/* 发送类型 */

	/* 物流名称 不存库 */
	private String FName;
	/* 物流代码 不存库 */
	private String FNumber;
	/* 表头FbillNo */
	private String fbillNo;
	/* 采购订单内码，由其他账套采购订单下推生产账套销售订单存在 */
	private String FEntrySelfS0168;
	/* 采购订单号 */
	private String FEntrySelfS0169;
	/* 采购订单分录码 */
	private String FEntrySelfS0170;
	/* 销售订单号 */
	private String FEntrySelfS0171;
	/* 销售订单内码 */
	private String FEntrySelfS0172;
	/* 销售订单分录码 */
	private String FEntrySelfS0173;
	private SeOrder seOrder;
	private ICItem cItem;

	public String getFbillNo() {
		return fbillNo;
	}

	public void setFbillNo(String fbillNo) {
		this.fbillNo = fbillNo;
	}

	public String getFEntrySelfS0169() {
		return FEntrySelfS0169;
	}

	public void setFEntrySelfS0169(String fEntrySelfS0169) {
		FEntrySelfS0169 = fEntrySelfS0169;
	}

	public String getFEntrySelfS0170() {
		return FEntrySelfS0170;
	}

	public void setFEntrySelfS0170(String fEntrySelfS0170) {
		FEntrySelfS0170 = fEntrySelfS0170;
	}

	public String getFEntrySelfS0171() {
		return FEntrySelfS0171;
	}

	public void setFEntrySelfS0171(String fEntrySelfS0171) {
		FEntrySelfS0171 = fEntrySelfS0171;
	}

	public String getFEntrySelfS0172() {
		return FEntrySelfS0172;
	}

	public void setFEntrySelfS0172(String fEntrySelfS0172) {
		FEntrySelfS0172 = fEntrySelfS0172;
	}

	public String getFEntrySelfS0173() {
		return FEntrySelfS0173;
	}

	public void setFEntrySelfS0173(String fEntrySelfS0173) {
		FEntrySelfS0173 = fEntrySelfS0173;
	}

	public String getFName() {
		return FName;
	}

	public void setFName(String fName) {
		FName = fName;
	}

	public String getFNumber() {
		return FNumber;
	}

	public void setFNumber(String fNumber) {
		FNumber = fNumber;
	}

	public int getFdetailid() {
		return fdetailid;
	}

	public void setFdetailid(int fdetailid) {
		this.fdetailid = fdetailid;
	}

	public String getFbrno() {
		return fbrno;
	}

	public void setFbrno(String fbrno) {
		this.fbrno = fbrno == null ? null : fbrno.trim();
	}

	public int getFinterid() {
		return finterid;
	}

	public void setFinterid(int finterid) {
		this.finterid = finterid;
	}

	public int getFentryid() {
		return fentryid;
	}

	public void setFentryid(int fentryid) {
		this.fentryid = fentryid;
	}

	public int getFitemid() {
		return fitemid;
	}

	public void setFitemid(int fitemid) {
		this.fitemid = fitemid;
	}

	public double getFqty() {
		return fqty;
	}

	public void setFqty(double fqty) {
		this.fqty = fqty;
	}

	public double getFcommitqty() {
		return fcommitqty;
	}

	public void setFcommitqty(double fcommitqty) {
		this.fcommitqty = fcommitqty;
	}

	public double getFprice() {
		return fprice;
	}

	public void setFprice(double fprice) {
		this.fprice = fprice;
	}

	public double getFamount() {
		return famount;
	}

	public void setFamount(double famount) {
		this.famount = famount;
	}

	public double getFtaxrate() {
		return ftaxrate;
	}

	public void setFtaxrate(double ftaxrate) {
		this.ftaxrate = ftaxrate;
	}

	public double getFtaxamount() {
		return ftaxamount;
	}

	public void setFtaxamount(double ftaxamount) {
		this.ftaxamount = ftaxamount;
	}

	public double getFtax() {
		return ftax;
	}

	public void setFtax(double ftax) {
		this.ftax = ftax;
	}

	public Float getFdiscount() {
		return fdiscount;
	}

	public void setFdiscount(Float fdiscount) {
		this.fdiscount = fdiscount;
	}

	public String getFnote() {
		return fnote;
	}

	public void setFnote(String fnote) {
		this.fnote = fnote == null ? null : fnote.trim();
	}

	public Date getFdate() {
		return fdate;
	}

	public void setFdate(Date fdate) {
		this.fdate = fdate;
	}

	public double getFdiscountamount() {
		return fdiscountamount;
	}

	public void setFdiscountamount(double fdiscountamount) {
		this.fdiscountamount = fdiscountamount;
	}

	public double getFinvoiceqty() {
		return finvoiceqty;
	}

	public void setFinvoiceqty(double finvoiceqty) {
		this.finvoiceqty = finvoiceqty;
	}

	public double getFbcommitqty() {
		return fbcommitqty;
	}

	public void setFbcommitqty(double fbcommitqty) {
		this.fbcommitqty = fbcommitqty;
	}

	public int getFtranleadtime() {
		return ftranleadtime;
	}

	public void setFtranleadtime(int ftranleadtime) {
		this.ftranleadtime = ftranleadtime;
	}

	public int getFatpdeduct() {
		return fatpdeduct;
	}

	public void setFatpdeduct(int fatpdeduct) {
		this.fatpdeduct = fatpdeduct;
	}

	public int getFcostobjectid() {
		return fcostobjectid;
	}

	public void setFcostobjectid(int fcostobjectid) {
		this.fcostobjectid = fcostobjectid;
	}

	public int getFunitid() {
		return funitid;
	}

	public void setFunitid(int funitid) {
		this.funitid = funitid;
	}

	public double getFauxbcommitqty() {
		return fauxbcommitqty;
	}

	public void setFauxbcommitqty(double fauxbcommitqty) {
		this.fauxbcommitqty = fauxbcommitqty;
	}

	public double getFauxcommitqty() {
		return fauxcommitqty;
	}

	public void setFauxcommitqty(double fauxcommitqty) {
		this.fauxcommitqty = fauxcommitqty;
	}

	public double getFauxinvoiceqty() {
		return fauxinvoiceqty;
	}

	public void setFauxinvoiceqty(double fauxinvoiceqty) {
		this.fauxinvoiceqty = fauxinvoiceqty;
	}

	public double getFauxprice() {
		return fauxprice;
	}

	public void setFauxprice(double fauxprice) {
		this.fauxprice = fauxprice;
	}

	public double getFauxqty() {
		return fauxqty;
	}

	public void setFauxqty(double fauxqty) {
		this.fauxqty = fauxqty;
	}

	public double getFunidiscount() {
		return funidiscount;
	}

	public void setFunidiscount(double funidiscount) {
		this.funidiscount = funidiscount;
	}

	public double getFfinalamount() {
		return ffinalamount;
	}

	public void setFfinalamount(double ffinalamount) {
		this.ffinalamount = ffinalamount;
	}

	public int getFsourceentryid() {
		return fsourceentryid;
	}

	public void setFsourceentryid(int fsourceentryid) {
		this.fsourceentryid = fsourceentryid;
	}

	public int getFhavemrp() {
		return fhavemrp;
	}

	public void setFhavemrp(int fhavemrp) {
		this.fhavemrp = fhavemrp;
	}

	public double getFstockqty() {
		return fstockqty;
	}

	public void setFstockqty(double fstockqty) {
		this.fstockqty = fstockqty;
	}

	public double getFauxstockqty() {
		return fauxstockqty;
	}

	public void setFauxstockqty(double fauxstockqty) {
		this.fauxstockqty = fauxstockqty;
	}

	public String getFbatchno() {
		return fbatchno;
	}

	public void setFbatchno(String fbatchno) {
		this.fbatchno = fbatchno == null ? null : fbatchno.trim();
	}

	public double getFcess() {
		return fcess;
	}

	public void setFcess(double fcess) {
		this.fcess = fcess;
	}

	public Date getFadviceconsigndate() {
		return fadviceconsigndate;
	}

	public void setFadviceconsigndate(Date fadviceconsigndate) {
		this.fadviceconsigndate = fadviceconsigndate;
	}

	public int getFbominterid() {
		return fbominterid;
	}

	public void setFbominterid(int fbominterid) {
		this.fbominterid = fbominterid;
	}

	public String getFmapnumber() {
		return fmapnumber;
	}

	public void setFmapnumber(String fmapnumber) {
		this.fmapnumber = fmapnumber == null ? null : fmapnumber.trim();
	}

	public String getFmapname() {
		return fmapname;
	}

	public void setFmapname(String fmapname) {
		this.fmapname = fmapname == null ? null : fmapname.trim();
	}

	public int getFlockflag() {
		return flockflag;
	}

	public void setFlockflag(int flockflag) {
		this.flockflag = flockflag;
	}

	public int getFinforecast() {
		return finforecast;
	}

	public void setFinforecast(int finforecast) {
		this.finforecast = finforecast;
	}

	public double getFallstdamount() {
		return fallstdamount;
	}

	public void setFallstdamount(double fallstdamount) {
		this.fallstdamount = fallstdamount;
	}

	public int getFauxpropid() {
		return fauxpropid;
	}

	public void setFauxpropid(int fauxpropid) {
		this.fauxpropid = fauxpropid;
	}

	public double getFauxpricediscount() {
		return fauxpricediscount;
	}

	public void setFauxpricediscount(double fauxpricediscount) {
		this.fauxpricediscount = fauxpricediscount;
	}

	public double getFtaxamt() {
		return ftaxamt;
	}

	public void setFtaxamt(double ftaxamt) {
		this.ftaxamt = ftaxamt;
	}

	public int getFsourcetrantype() {
		return fsourcetrantype;
	}

	public void setFsourcetrantype(int fsourcetrantype) {
		this.fsourcetrantype = fsourcetrantype;
	}

	public int getFsourceinterid() {
		return fsourceinterid;
	}

	public void setFsourceinterid(int fsourceinterid) {
		this.fsourceinterid = fsourceinterid;
	}

	public String getFsourcebillno() {
		return fsourcebillno;
	}

	public void setFsourcebillno(String fsourcebillno) {
		this.fsourcebillno = fsourcebillno;
	}

	public int getFcontractinterid() {
		return fcontractinterid;
	}

	public void setFcontractinterid(int fcontractinterid) {
		this.fcontractinterid = fcontractinterid;
	}

	public int getFcontractentryid() {
		return fcontractentryid;
	}

	public void setFcontractentryid(int fcontractentryid) {
		this.fcontractentryid = fcontractentryid;
	}

	public String getFcontractbillno() {
		return fcontractbillno;
	}

	public void setFcontractbillno(String fcontractbillno) {
		this.fcontractbillno = fcontractbillno;
	}

	public double getFcommitinstall() {
		return fcommitinstall;
	}

	public void setFcommitinstall(double fcommitinstall) {
		this.fcommitinstall = fcommitinstall;
	}

	public double getFauxcommitinstall() {
		return fauxcommitinstall;
	}

	public void setFauxcommitinstall(double fauxcommitinstall) {
		this.fauxcommitinstall = fauxcommitinstall;
	}

	public double getFseccommitinstall() {
		return fseccommitinstall;
	}

	public void setFseccommitinstall(double fseccommitinstall) {
		this.fseccommitinstall = fseccommitinstall;
	}

	public int getFplanmode() {
		return fplanmode;
	}

	public void setFplanmode(int fplanmode) {
		this.fplanmode = fplanmode;
	}

	public String getFmtono() {
		return fmtono;
	}

	public void setFmtono(String fmtono) {
		this.fmtono = fmtono;
	}

	public String getForderbillno() {
		return forderbillno;
	}

	public void setForderbillno(String forderbillno) {
		this.forderbillno = forderbillno;
	}

	public int getForderentryid() {
		return forderentryid;
	}

	public void setForderentryid(int forderentryid) {
		this.forderentryid = forderentryid;
	}

	public int getFbomcategory() {
		return fbomcategory;
	}

	public void setFbomcategory(int fbomcategory) {
		this.fbomcategory = fbomcategory;
	}

	public int getForderbomstatus() {
		return forderbomstatus;
	}

	public void setForderbomstatus(int forderbomstatus) {
		this.forderbomstatus = forderbomstatus;
	}

	public int getForderbominterid() {
		return forderbominterid;
	}

	public void setForderbominterid(int forderbominterid) {
		this.forderbominterid = forderbominterid;
	}

	public int getFoutsourceinterid() {
		return foutsourceinterid;
	}

	public void setFoutsourceinterid(int foutsourceinterid) {
		this.foutsourceinterid = foutsourceinterid;
	}

	public int getFoutsourceentryid() {
		return foutsourceentryid;
	}

	public void setFoutsourceentryid(int foutsourceentryid) {
		this.foutsourceentryid = foutsourceentryid;
	}

	public int getFoutsourcetrantype() {
		return foutsourcetrantype;
	}

	public void setFoutsourcetrantype(int foutsourcetrantype) {
		this.foutsourcetrantype = foutsourcetrantype;
	}

	public String getFEntrySelfS0168() {
		return FEntrySelfS0168;
	}

	public void setFEntrySelfS0168(String fEntrySelfS0168) {
		FEntrySelfS0168 = fEntrySelfS0168;
	}

	public SeOrder getSeOrder() {
		return seOrder;
	}

	public void setSeOrder(SeOrder seOrder) {
		this.seOrder = seOrder;
	}

	public ICItem getcItem() {
		return cItem;
	}

	public void setcItem(ICItem cItem) {
		this.cItem = cItem;
	}

	@Override
	public String toString() {
		return "SeOrderEntry [fdetailid=" + fdetailid + ", fbrno=" + fbrno + ", finterid=" + finterid + ", fentryid="
				+ fentryid + ", fitemid=" + fitemid + ", fqty=" + fqty + ", fcommitqty=" + fcommitqty + ", fprice="
				+ fprice + ", famount=" + famount + ", ftaxrate=" + ftaxrate + ", ftaxamount=" + ftaxamount + ", ftax="
				+ ftax + ", fdiscount=" + fdiscount + ", fnote=" + fnote + ", fdate=" + fdate + ", fdiscountamount="
				+ fdiscountamount + ", finvoiceqty=" + finvoiceqty + ", fbcommitqty=" + fbcommitqty + ", ftranleadtime="
				+ ftranleadtime + ", fatpdeduct=" + fatpdeduct + ", fcostobjectid=" + fcostobjectid + ", funitid="
				+ funitid + ", fauxbcommitqty=" + fauxbcommitqty + ", fauxcommitqty=" + fauxcommitqty
				+ ", fauxinvoiceqty=" + fauxinvoiceqty + ", fauxprice=" + fauxprice + ", fauxqty=" + fauxqty
				+ ", funidiscount=" + funidiscount + ", ffinalamount=" + ffinalamount + ", fsourceentryid="
				+ fsourceentryid + ", fhavemrp=" + fhavemrp + ", fstockqty=" + fstockqty + ", fauxstockqty="
				+ fauxstockqty + ", fbatchno=" + fbatchno + ", fcess=" + fcess + ", fadviceconsigndate="
				+ fadviceconsigndate + ", fbominterid=" + fbominterid + ", fmapnumber=" + fmapnumber + ", fmapname="
				+ fmapname + ", flockflag=" + flockflag + ", finforecast=" + finforecast + ", fallstdamount="
				+ fallstdamount + ", fauxpropid=" + fauxpropid + ", fauxpricediscount=" + fauxpricediscount
				+ ", ftaxamt=" + ftaxamt + ", fsourcetrantype=" + fsourcetrantype + ", fsourceinterid=" + fsourceinterid
				+ ", fsourcebillno=" + fsourcebillno + ", fcontractinterid=" + fcontractinterid + ", fcontractentryid="
				+ fcontractentryid + ", fcontractbillno=" + fcontractbillno + ", fcommitinstall=" + fcommitinstall
				+ ", fauxcommitinstall=" + fauxcommitinstall + ", fseccommitinstall=" + fseccommitinstall
				+ ", fplanmode=" + fplanmode + ", fmtono=" + fmtono + ", forderbillno=" + forderbillno
				+ ", forderentryid=" + forderentryid + ", fbomcategory=" + fbomcategory + ", forderbomstatus="
				+ forderbomstatus + ", forderbominterid=" + forderbominterid + ", foutsourceinterid="
				+ foutsourceinterid + ", foutsourceentryid=" + foutsourceentryid + ", foutsourcetrantype="
				+ foutsourcetrantype + ", FName=" + FName + ", FNumber=" + FNumber + ", fbillNo=" + fbillNo
				+ ", FEntrySelfS0168=" + FEntrySelfS0168 + ", FEntrySelfS0169=" + FEntrySelfS0169 + ", FEntrySelfS0170="
				+ FEntrySelfS0170 + ", FEntrySelfS0171=" + FEntrySelfS0171 + ", FEntrySelfS0172=" + FEntrySelfS0172
				+ ", FEntrySelfS0173=" + FEntrySelfS0173 + ", seOrder=" + seOrder + ", cItem=" + cItem + "]";
	}


}