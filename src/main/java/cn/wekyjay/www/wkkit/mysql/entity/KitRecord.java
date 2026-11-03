package cn.wekyjay.www.wkkit.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "kit", keepGlobalPrefix = true)
public class KitRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String kitname;
    @TableField("group_id")
    private Long groupId;
    private String config;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKitname() { return kitname; }
    public void setKitname(String kitname) { this.kitname = kitname; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
}
