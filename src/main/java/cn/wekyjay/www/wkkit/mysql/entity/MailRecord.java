package cn.wekyjay.www.wkkit.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "maildata", keepGlobalPrefix = true)
public class MailRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String player;
    @TableField("kitname")
    private String kitname;
    private Integer num;
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }
    public String getKitname() { return kitname; }
    public void setKitname(String kitname) { this.kitname = kitname; }
    public Integer getNum() { return num; }
    public void setNum(Integer num) { this.num = num; }
}
