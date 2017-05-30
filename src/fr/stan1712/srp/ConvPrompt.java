package fr.stan1712.srp;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class ConvPrompt extends StringPrompt{

	
	@Override
	public Prompt acceptInput(ConversationContext con, String answer){
		con.getForWhom().sendRawMessage("Cool! I like "+ answer +" too!");
	return null;
	}
	
	@Override
	public String getPromptText(ConversationContext arg0){
		
	return "What is you favorite color ?";
	}
}
