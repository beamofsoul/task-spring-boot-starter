package com.beamofsoul.cloud.execute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.beamofsoul.cloud.DistributedAutomaticTaskingServerConfiguration;
import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingServer;
import com.beamofsoul.cloud.execute.CommandMessage.CommandType;
import com.beamofsoul.cloud.register.RegistedTaskContainer;

@ConditionalOnAnnotation(EnableTaskingServer.class)
@Component
public class ServerCommandExecutor {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServerCommandExecutor.class);
	
	public Boolean execute(String application, String instance, CommandMessage commandMessage) {
		if (commandMessage.getCommand().equals(CommandType.REGISTER.getValue())) {
			register(application, instance, commandMessage.getMessage());
		} else if (commandMessage.getCommand().equals(CommandType.INVOKE.getValue())) {
			invoke(application, instance, commandMessage.getMessage());
			return null;
		} else {
			return false;
		}
		return true;
	}
	
	private void register(String application, String instance, String message) {
		HashMap<String, List<String>> tasks = JSONObject.parseObject(message, new TypeReference<HashMap<String, List<String>>>() {});
		Map<String, Map<String, List<String>>> tempTaskMap = new HashMap<>();
		
		String applicationInstance = application + ":" + instance;
		for (Map.Entry<String, List<String>> task : tasks.entrySet()) {
			tempTaskMap.put(task.getKey(), new HashMap<String, List<String>>() {
				private static final long serialVersionUID = 1L;
			{
				put(applicationInstance, task.getValue());
			}});
		}
		
		RegistedTaskContainer.putAll(tempTaskMap);
		DistributedAutomaticTaskingServerConfiguration.getBean(LoopInvoker.class).prepareNextDates();
	}
	
	private void invoke(String application, String instance, String message) {
		String[] messages = message.split(":");
		String taskKey = messages[0];
		boolean flag = Boolean.parseBoolean(messages[1]);
		LOG.info(String.format("%s: %s -> %s", flag ? "success" : "failure", application + ":" + instance, taskKey));
	}
}
