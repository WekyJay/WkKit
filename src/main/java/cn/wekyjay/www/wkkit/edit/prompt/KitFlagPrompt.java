package cn.wekyjay.www.wkkit.edit.prompt;

import cn.wekyjay.www.wkkit.WkKit;
import cn.wekyjay.www.wkkit.kit.Kit;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class KitFlagPrompt {
	// 添加Flag
	public static void setFlag(Player player, String kitname, String flag) {
		Conversation conversation = new ConversationFactory(WkKit.getWkKit())
				.withFirstPrompt(new KitFlagPrompt_SetFlag())
				.withTimeout(60)
				.buildConversation(player);
		conversation.getContext().setSessionData("kitname", kitname);
		conversation.getContext().setSessionData("flag", flag);
		conversation.begin();
	}
	// 删除Flag
	public static void deFlag(Player player, String kitname, String flag) {
		Conversation conversation = new ConversationFactory(WkKit.getWkKit())
				.withFirstPrompt(new KitFlagPrompt_DeFlag())
				.withTimeout(60)
				.buildConversation(player);
		conversation.getContext().setSessionData("kitname", kitname);
		conversation.getContext().setSessionData("flag", flag);
		conversation.begin();
	}

}
class KitFlagPrompt_SetFlag extends ValidatingPrompt{

	@Override
	public String getPromptText(ConversationContext context) {
		String flag = (String) context.getSessionData("flag");
		String kitname = (String) context.getSessionData("kitname");
		return "§a正在修改§e" + kitname + "§a的§e" + flag + "§a ,请输入你要修改的值(Cancel取消):";
	}

	@Override
	protected boolean isInputValid(ConversationContext context, String input) {
	    String flag = (String)context.getSessionData("flag");
	    if (flag.equals("Delay") && (input.equalsIgnoreCase("Cancel") || !input.matches("[0-9]+")))
	      return false; 
	    if (flag.equals("Times") && (input.equalsIgnoreCase("Cancel") || !input.matches("[0-9]+")))
	      return false;
	    if (flag.equals("Vault") && (input.equalsIgnoreCase("Cancel") || !input.matches("[0-9]+")))
	      return false;
	    if (flag.equals("NoRefreshFirst") && !input.equalsIgnoreCase("Cancel") && !input.equalsIgnoreCase("true") && !input.equalsIgnoreCase("false"))
	      return false;
	    return (input.length() > 0);
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		if(input.equalsIgnoreCase("Cancel")) {
			context.getForWhom().sendRawMessage("§e你取消了！");
			return Prompt.END_OF_CONVERSATION;
		}
		input = input.replace("&", "§");
		String flag = (String) context.getSessionData("flag");
		String kitname = (String) context.getSessionData("kitname");
		Kit kit = Kit.getKit(kitname);
		switch(flag) {
		case "DisplayName": kit.setDisplayName(input.replaceAll("&", "§"));break;
		case "Icon": kit.setIcon(input);break;
		case "Permission": kit.setPermission(input);break;
		case "Times": kit.setTimes(Integer.parseInt(input));break;
		case "Delay": kit.setDelay(Integer.parseInt(input));break;
		case "DoCron": kit.setDocron(input);break;
		case "Lore" : kit.setLore(Arrays.asList(input.replaceAll("&", "§").split(",")));break;
		case "MythicMobs" : kit.setMythicMobs(Arrays.asList(input.replaceAll("&", "§").split(",")));break;
		case "Drop": kit.setDrop(Arrays.asList(input.split(",")));break;
		case "Commands": kit.setCommands(Arrays.asList(input.replaceAll("&", "§").split(",")));break;
		case "Vault": kit.setVault(Integer.parseInt(input));break;
		case "NoRefreshFirst": kit.setNoRefreshFirst(Boolean.parseBoolean(input));break;
		}
		kit.saveConfig(); // 保存礼包配置
		context.getForWhom().sendRawMessage("§a修改成功！");
		return Prompt.END_OF_CONVERSATION;
	}
	
}
class KitFlagPrompt_DeFlag extends ValidatingPrompt{

	@Override
	public String getPromptText(ConversationContext context) {
		String flag = (String) context.getSessionData("flag");
		String kitname = (String) context.getSessionData("kitname");
		return "§c你确定要删除§e" + kitname + "§c的§e" + flag + "§c吗？(Y/N)";
	}

	@Override
	protected boolean isInputValid(ConversationContext context, String input) {
		if(input.equalsIgnoreCase("Y") || input.equals("N")) return true; 
		return false; 
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		if(input.equalsIgnoreCase("N")) {
			context.getForWhom().sendRawMessage("§e你取消了！");
			return Prompt.END_OF_CONVERSATION;
		}
		String flag = (String) context.getSessionData("flag");
		String kitname = (String) context.getSessionData("kitname");
		Kit kit = Kit.getKit(kitname);
		switch(flag) {
		case "DisplayName": kit.setDisplayName(WkKit.getWkKit().getConfig().getString("Default.Name"));break;
		case "Icon": kit.setIcon(WkKit.getWkKit().getConfig().getString("Default.Icon"));break;
		case "Permission": kit.setPermission(null);break;
		case "Times": kit.setTimes(-1);break;
		case "Delay": kit.setDelay(0);break;
		case "DoCron": kit.setDocron(null);break;
		case "Lore" : kit.setLore(java.util.Collections.<String>emptyList());break;
		case "MythicMobs" : kit.setMythicMobs(java.util.Collections.<String>emptyList());break;
		case "Drop": kit.setDrop(java.util.Collections.<String>emptyList());break;
		case "Commands": kit.setCommands(java.util.Collections.<String>emptyList());break;
		case "Vault": kit.setVault(null);break;
		case "NoRefreshFirst": kit.setNoRefreshFirst(false);break;
		case "Item": kit.setItemStack(new org.bukkit.inventory.ItemStack[0]);break;
		}
		kit.saveConfig(); // 保存礼包配置
		context.getForWhom().sendRawMessage("§a删除成功！");
		return Prompt.END_OF_CONVERSATION;
	}
	
}
