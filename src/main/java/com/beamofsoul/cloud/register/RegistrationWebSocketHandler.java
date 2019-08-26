package com.beamofsoul.cloud.register;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.alibaba.fastjson.JSONObject;
import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingClient;
import com.beamofsoul.cloud.execute.ClientCommandExecutor;
import com.beamofsoul.cloud.execute.CommandMessage;
import com.beamofsoul.cloud.execute.CommandMessage.CommandType;

@ConditionalOnAnnotation(EnableTaskingClient.class)
@Component
public class RegistrationWebSocketHandler extends TextWebSocketHandler {
	
	public static WebSocketSession serverSession = null;
	
	@Autowired
	private ClientCommandExecutor commandExecutor;
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		RegistrationWebSocketHandler.serverSession = session;
		CommandMessage commandMessage = new CommandMessage(CommandType.REGISTER.getValue(), JSONObject.toJSONString(RegisterInformation.get()));
		session.sendMessage(new TextMessage(JSONObject.toJSONString(commandMessage)));
		super.afterConnectionEstablished(session);
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		CommandMessage commandMessage = CommandMessage.parse(message.getPayload());
		commandExecutor.execute(session, commandMessage);
		super.handleTextMessage(session, message);
	}
}
