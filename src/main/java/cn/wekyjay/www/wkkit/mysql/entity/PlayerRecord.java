package cn.wekyjay.www.wkkit.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "player", keepGlobalPrefix = true)
public class PlayerRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String player;
    @TableField("kitname")
    private String kitname;
    private String data;
    private Integer time;
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }
    public String getKitname() { return kitname; }
    public void setKitname(String kitname) { this.kitname = kitname; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }
}
