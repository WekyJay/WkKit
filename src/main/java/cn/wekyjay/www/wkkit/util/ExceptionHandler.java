package cn.wekyjay.www.wkkit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 统一异常处理工具类
 * 替换项目中所有的 ExceptionHandler.handle("未知操作", e) 调用
 */
public class ExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    
    private ExceptionHandler() {
        // 工具类，禁止实例化
    }
    
    /**
     * 通用异常处理
     * @param context 异常发生的上下文描述
     * @param e 异常对象
     */
    public static void handle(String context, Exception e) {
        logger.error("[WkKit] Error in {}: {}", context, e.getMessage(), e);
        
        // 可以根据异常类型发送不同的通知
        if (e instanceof IOException) {
            logger.warn("[WkKit] IO操作失败，请检查文件权限或磁盘空间");
        } else if (e instanceof SQLException) {
            logger.warn("[WkKit] 数据库操作失败，请检查数据库连接和配置");
        } else if (e instanceof NullPointerException) {
            logger.warn("[WkKit] 空指针异常，请检查代码逻辑");
        }
    }
    
    /**
     * IO异常专用处理
     * @param operation 具体的IO操作描述
     * @param e IO异常
     */
    public static void handleIO(String operation, IOException e) {
        logger.error("[WkKit] IO error during {}: {}", operation, e.getMessage(), e);
        logger.warn("[WkKit] 请检查文件路径：{}", e.getMessage().contains("Permission denied") ? 
                   "文件权限不足" : "文件可能被占用或路径不存在");
    }
    
    /**
     * SQL异常专用处理
     * @param operation 具体的SQL操作描述
     * @param e SQL异常
     */
    public static void handleSQL(String operation, SQLException e) {
        logger.error("[WkKit] SQL error during {}: {}", operation, e.getMessage(), e);
        logger.warn("[WkKit] SQL状态码：{}，错误码：{}", e.getSQLState(), e.getErrorCode());
        
        // 根据常见错误码提供建议
        switch (e.getErrorCode()) {
            case 1045:
                logger.warn("[WkKit] 数据库用户名或密码错误");
                break;
            case 1049:
                logger.warn("[WkKit] 数据库不存在，请检查数据库名称");
                break;
            case 2003:
                logger.warn("[WkKit] 无法连接到数据库服务器，请检查IP和端口");
                break;
            case 1146:
                logger.warn("[WkKit] 数据表不存在，请运行插件初始化表结构");
                break;
        }
    }
    
    /**
     * 配置异常处理
     * @param configFile 配置文件路径
     * @param e 异常
     */
    public static void handleConfig(String configFile, Exception e) {
        logger.error("[WkKit] 配置文件 {} 加载失败: {}", configFile, e.getMessage(), e);
        logger.warn("[WkKit] 请检查配置文件格式是否正确，或删除配置文件让插件重新生成");
    }
    
    /**
     * 网络异常处理
     * @param url 访问的URL
     * @param e 异常
     */
    public static void handleNetwork(String url, Exception e) {
        logger.error("[WkKit] 网络请求失败 {}: {}", url, e.getMessage(), e);
        logger.warn("[WkKit] 请检查网络连接或目标服务器状态");
    }
    
    /**
     * 插件钩子异常处理
     * @param pluginName 插件名称
     * @param hookType 钩子类型
     * @param e 异常
     */
    public static void handleHook(String pluginName, String hookType, Exception e) {
        logger.error("[WkKit] 插件 {} 的 {} 钩子加载失败: {}", 
                    pluginName, hookType, e.getMessage(), e);
        logger.warn("[WkKit] {} 插件可能版本不兼容或未正确安装", pluginName);
    }
    
    /**
     * 静默处理（仅记录不抛出）
     * @param context 上下文
     * @param e 异常
     */
    public static void handleSilently(String context, Exception e) {
        logger.debug("[WkKit] Silent error in {}: {}", context, e.getMessage());
    }
    
    /**
     * 警告级别处理
     * @param context 上下文
     * @param e 异常
     */
    public static void handleWarning(String context, Exception e) {
        logger.warn("[WkKit] Warning in {}: {}", context, e.getMessage(), e);
    }
    
    /**
     * 获取异常根原因
     * @param e 异常
     * @return 根原因异常信息
     */
    public static String getRootCause(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}