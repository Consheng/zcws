package ykk.cb.com.zcws.bean;

/**
 * @author lin
 * @createDate 2021-04-20 11:22
 * @brief :
 */
public class OrderProcess {
    private int id;
    /**
     * 选择节点id
     */
    private int nodeId;
    /**
     * 节点顺序
     */
    private String seq;
    /**
     * 是否判断节点
     */
    private String flagNode;
    /**
     * 判断提示
     */
    private String tips;
    /**
     * 选择节点id
     */
    private int flagValue;
    /**
     * 临时字段
     */
    private String nodeName; //节点名称
    private String flagValueName; //节点名称

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getFlagValueName() {
        return flagValueName;
    }

    public void setFlagValueName(String flagValueName) {
        this.flagValueName = flagValueName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getFlagNode() {
        return flagNode;
    }

    public void setFlagNode(String flagNode) {
        this.flagNode = flagNode;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getFlagValue() {
        return flagValue;
    }

    public void setFlagValue(int flagValue) {
        this.flagValue = flagValue;
    }
}
