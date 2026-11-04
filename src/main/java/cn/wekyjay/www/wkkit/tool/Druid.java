package cn.wekyjay.www.wkkit.tool;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接池管理器
 */
public class Druid {
    private static DruidDataSource druidDataSource;


    // 初始化连接池
    public Druid(String url,String userName, String userPassword){
        this(url, userName, userPassword, "com.mysql.cj.jdbc.Driver", false);
    }

    public Druid(String url, String userName, String userPassword, String driverClass, boolean sqlite){

        druidDataSource=new DruidDataSource();
        druidDataSource.setUrl(url);
        druidDataSource.setDriverClassName(driverClass);
        druidDataSource.setUsername(userName);
        druidDataSource.setPassword(userPassword);
        //可选设置
        druidDataSource.setMaxActive(sqlite ? 1 : 20);
        druidDataSource.setInitialSize(sqlite ? 1 : 10);
        druidDataSource.setMaxWait(5000);
        druidDataSource.setValidationQuery("SELECT 1");
        druidDataSource.setTestWhileIdle(true);

    }
    //获取连接
    public static Connection getConnection() throws SQLException {
        try {
            DruidPooledConnection dds = druidDataSource.getConnection();
            MessageManager.infoDeBug("当前连接池：" + dds.toString());
            return dds;

        } catch (SQLException e) {
            //如果连接出现异常，则抛出一个运行时异常
            throw new RuntimeException("连接池出现异常.");
        }
    }

    public static DataSource getDataSource() {
        return druidDataSource;
    }

    public static void shutdown(){
            druidDataSource.close();
    }

}
