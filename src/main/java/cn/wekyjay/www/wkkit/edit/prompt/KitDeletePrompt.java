package cn.wekyjay.www.wkkit.edit.prompt;

import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

import cn.wekyjay.www.wkkit.WkKit;

public class KitDeletePrompt {
	public static void newConversation(Player player, String kitname) {
		Conversation conversation = new ConversationFactory(WkKit.getWkKit()) // ����һ���Ự
	            .withFirstPrompt(new KitDeletePrompt_1()) // �������������ɵĶԻ���ʹ�õĵ�һ����ʾ��
	            .withTimeout(60)
	            .buildConversation(player);
		conversation.getContext().setSessionData("kitname", kitname);
		conversation.begin();
	}
}
class KitDeletePrompt_1 extends ValidatingPrompt{

	@Override
	public String getPromptText(ConversationContext context) {
		return "���Ƿ�Ҫɾ����e" + context.getSessionData("kitname") + "��f?(Y/N)";
	}

	@Override
	protected boolean isInputValid(ConversationContext context, String input) {
		if(input.equalsIgnoreCase("Y") || input.equals("N")) return true; 
		return false; 
	}

	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String input) {
		if(input.equalsIgnoreCase("Y")) {
			Bukkit.dispatchCommand((Player)context.getForWhom(), "wk delete " + context.getSessionData("kitname"));
			context.getForWhom().sendRawMessage("��a�ѳɹ�ɾ����� - " + context.getSessionData("kitname"));
		}else {
			context.getForWhom().sendRawMessage("��c��ȡ�������ɾ��");
		}
		return Prompt.END_OF_CONVERSATION;
	}
	
}