package cn.wekyjay.www.wkkit.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "cdk", keepGlobalPrefix = true)
public class CdkRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String cdk;
    private String kits;
    private String date;
    private String status;
    private String mark;
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCdk() { return cdk; }
    public void setCdk(String cdk) { this.cdk = cdk; }
    public String getKits() { return kits; }
    public void setKits(String kits) { this.kits = kits; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMark() { return mark; }
    public void setMark(String mark) { this.mark = mark; }
}
