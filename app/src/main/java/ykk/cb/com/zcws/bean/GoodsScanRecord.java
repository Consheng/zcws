package ykk.cb.com.zcws.bean;

/**
 * @author lin
 * @createDate 2021-04-23 13:45
 * @brief :
 */
public class GoodsScanRecord {

    private int id;
    /**
     *  发货单号
     */
    private String deliveryNo;
    /**
     * 节点
     */
    private int nodeId;
    /**
     *  用户
     */
    private int userId;
    /**
     * 创建时间
     */
    private String createDate;

    //临时字段
    private String nodeName; //节点名称
    private String userName;//用户名称


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeliveryNo() {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo) {
        this.deliveryNo = deliveryNo;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
