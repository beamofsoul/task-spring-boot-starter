package com.beamofsoul.cloud.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.beamofsoul.cloud.DistributedAutomaticTaskingServerConfiguration;
import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingServer;
import com.beamofsoul.cloud.execute.CommandMessage;
import com.beamofsoul.cloud.execute.ServerCommandExecutor;
import com.beamofsoul.cloud.register.RegistedTaskContainer;

/**
 * @className:  WebSocketServer   
 * @description: WebSocket服务端事件响应机制类
 * @author: Mingshu Jian
 * @date: 2019年7月30日 下午3:34:57
 */
@ConditionalOnAnnotation(EnableTaskingServer.class)
@ServerEndpoint(value = "/websocket/{application}/{instance}")
@Component
public class WebSocketServer {

	private static final AtomicInteger COUNT = new AtomicInteger(0);
	private static final Map<String, Map<String, Session>> SESSIONS = new ConcurrentHashMap<>();
	
	public void sendMessage(String application, String instance, CommandMessage commandMessage) {
		try {
			Session currentSession = SESSIONS.get(application).get(instance);
			currentSession.getBasicRemote().sendText(JSONObject.toJSONString(commandMessage));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@OnOpen
	public void onOpen(@PathParam("application") String application, @PathParam("instance") String instance, Session session, EndpointConfig config) {
		if (SESSIONS.containsKey(application)) {
			SESSIONS.get(application).put(instance, session);
		} else {
			SESSIONS.put(application, new HashMap<String, Session>() {
				private static final long serialVersionUID = 1L;
			{
				put(instance, session);
			}});
		}
		incrementAndGetCount();
	}

	@OnClose
	public void onClose(@PathParam("application") String application, @PathParam("instance") String instance) {
		RegistedTaskContainer.remove(application, instance);
		if (SESSIONS.containsKey(application)) {
			if (SESSIONS.get(application).size() == 1) {
				SESSIONS.remove(application);
			} else {
				SESSIONS.get(application).remove(instance);
			}
		}
		decrementAndGetCount();
	}

	@OnMessage
	public void onMessage(@PathParam("application") String application, @PathParam("instance") String instance, String message, Session session) {
		try {
			ServerCommandExecutor executor = DistributedAutomaticTaskingServerConfiguration.getBean(ServerCommandExecutor.class);
			CommandMessage commandMessage = CommandMessage.parse(message);
			Boolean flag = executor.execute(application, instance, commandMessage);
			if (flag != null) {
				session.getBasicRemote().sendText(JSONObject.toJSONString(new CommandMessage(commandMessage.getCommand(), String.valueOf(flag))));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnError
	public void onError(Session session, Throwable error) {
		error.printStackTrace();
	}

	public static int getCount() {
		return COUNT.get();
	}

	public static int incrementAndGetCount() {
		return COUNT.incrementAndGet();
	}

	public static int decrementAndGetCount() {
		return COUNT.decrementAndGet();
	}
}
