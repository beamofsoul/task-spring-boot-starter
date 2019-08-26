package com.beamofsoul.cloud.execute;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSONObject;
import com.beamofsoul.cloud.DistributedAutomaticTaskingClientConfiguration;
import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingClient;
import com.beamofsoul.cloud.execute.CommandMessage.CommandType;

@ConditionalOnAnnotation(EnableTaskingClient.class)
@Component
public class ClientCommandExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(ClientCommandExecutor.class);
	private static final Map<Long, Set<String>> INVOKED_TASK_MAP = new ConcurrentHashMap<>();
	
	public void execute(WebSocketSession session, CommandMessage commandMessage) {
		if (commandMessage.getCommand().equals(CommandType.REGISTER.getValue())) {
			register(session, commandMessage);
		} else if (commandMessage.getCommand().equals(CommandType.INVOKE.getValue())) {
			invoke(session, commandMessage);
		}
	}

	private void register(WebSocketSession session, CommandMessage commandMessage) {
		boolean isRegisted = Boolean.parseBoolean(commandMessage.getMessage());
		LOG.info(isRegisted ? "Successfully registered on {}" : "Register failed({})", isRegisted ? session.getRemoteAddress() : commandMessage.getMessage());
	}
	
	private void invoke(WebSocketSession session, CommandMessage commandMessage) {
		String taskKey = commandMessage.getMessage();
		boolean invoked = false;
		
		if (isFirst(taskKey)) {
			MethodBean methodBean = DistributedAutomaticTaskingClientConfiguration.getMethodBean(taskKey);
			invoked = methodBean.invoke();
		}
		
		CommandMessage responseMessage = new CommandMessage(CommandType.INVOKE.getValue(), (taskKey + ":" + invoked));
		try {
			session.sendMessage(new TextMessage(JSONObject.toJSONString(responseMessage)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isFirst(String taskKey) {
		long currentSeconds = Instant.now().getEpochSecond();
		Set<String> taskSet = INVOKED_TASK_MAP.get(currentSeconds);
		if (taskSet == null || taskSet.isEmpty()) {
			INVOKED_TASK_MAP.put(currentSeconds, new HashSet<String>() {
				private static final long serialVersionUID = 1L;
			{
				add(taskKey);
			}});
			return true;
		} else {
			boolean isFirst = !taskSet.contains(taskKey);
			taskSet.add(taskKey);
			return isFirst;
		}
	}
}
