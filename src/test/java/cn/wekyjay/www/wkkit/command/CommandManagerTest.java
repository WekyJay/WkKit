package cn.wekyjay.www.wkkit.command.v2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandManager 单元测试
 * Day 5: 测试与质量保证
 */
public class CommandManagerTest {
    
    private CommandManager manager;
    
    @BeforeEach
    void setUp() {
        manager = CommandManager.getInstance();
    }
    
    @Test
    void testSingletonInstance() {
        CommandManager m1 = CommandManager.getInstance();
        CommandManager m2 = CommandManager.getInstance();
        assertSame(m1, m2);
    }
    
    @Test
    void testCommandRegistration() {
        BaseCommand infoCmd = new KitInfoCommand();
        manager.registerCommand(infoCmd);
        
        assertTrue(manager.hasCommand("info"));
        assertNotNull(manager.getCommand("info"));
    }
    
    @Test
    void testCommandAliases() {
        BaseCommand giveCmd = new KitGiveCommand();
        manager.registerCommand(giveCmd);
        
        // Should be accessible by main name and aliases
        assertTrue(manager.hasCommand("give"));
        // Aliases like "g" should also work if defined
    }
    
    @Test
    void testGetAllCommands() {
        assertNotNull(manager.getAllCommands());
    }
    
    @Test
    void testUnregisterCommand() {
        BaseCommand cmd = new KitInfoCommand();
        manager.registerCommand(cmd);
        assertTrue(manager.hasCommand("info"));
        
        manager.unregisterCommand("info");
        assertFalse(manager.hasCommand("info"));
    }
}
