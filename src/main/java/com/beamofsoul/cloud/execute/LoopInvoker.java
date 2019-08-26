package com.beamofsoul.cloud.execute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingServer;
import com.beamofsoul.cloud.balance.SmoothWeightedRoundRobin;
import com.beamofsoul.cloud.balance.Task;
import com.beamofsoul.cloud.execute.CommandMessage.CommandType;
import com.beamofsoul.cloud.register.RegistedTaskContainer;
import com.beamofsoul.cloud.socket.WebSocketServer;
import com.beamofsoul.cloud.util.CronDateUtils;

@ConditionalOnAnnotation(EnableTaskingServer.class)
@Component
public class LoopInvoker {
	
	public static final Logger LOG = LoggerFactory.getLogger(LoopInvoker.class);

	private static final Map<String, Set<String>> NEXT_DATE_CRON_EXPRESSION_MAP = new HashMap<>();
	
	@Autowired
	private WebSocketServer webSocketServer;
	
	public void prepareNextDate(String cronExpression) {
		String nextInvokeDatetime = CronDateUtils.getNextInvokeDatetime(cronExpression);
		if (NEXT_DATE_CRON_EXPRESSION_MAP.containsKey(nextInvokeDatetime)) {
			NEXT_DATE_CRON_EXPRESSION_MAP.get(nextInvokeDatetime).add(cronExpression);
		} else {
			NEXT_DATE_CRON_EXPRESSION_MAP.put(nextInvokeDatetime,  new HashSet<String>() {   
				private static final long serialVersionUID = 1L;
			{
				add(cronExpression);
			}});
		}
	}
	
	public void prepareNextDates() {
		Set<String> cronExpressions = RegistedTaskContainer.getCronExpressions();
		for (String cronExpression : cronExpressions) {
			prepareNextDate(cronExpression);
		}
	}
	
	public void loop() {
		if (NEXT_DATE_CRON_EXPRESSION_MAP.isEmpty()) {
			prepareNextDates();
		}
		while(true) {
			try {
				String currentDatetime = CronDateUtils.getCurrentDatetime();
				Set<String> cronExpressionSet = NEXT_DATE_CRON_EXPRESSION_MAP.get(currentDatetime);
				if (cronExpressionSet != null && !cronExpressionSet.isEmpty()) {
					invoke(currentDatetime, cronExpressionSet);
				}
				Thread.sleep(1 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void invoke(String currentDatetime, Set<String> cronExpressionSet) {
		for (String cronExpression : cronExpressionSet) {
			Map<Task, SmoothWeightedRoundRobin> taskRobinMap = RegistedTaskContainer.get(cronExpression);
			if (taskRobinMap == null || taskRobinMap.isEmpty()) {
				throw new RuntimeException(String.format("Cannot obtain related service APIs mapping information of target cron expression '%s' normally", cronExpression));
			}
			
			for (Entry<Task, SmoothWeightedRoundRobin> entry : taskRobinMap.entrySet()) {
				Task task = entry.getKey();
				SmoothWeightedRoundRobin robin = entry.getValue();
				String balancedInstanceName = robin.select().getInstanceName();
				new Thread(() -> {
					webSocketServer.sendMessage(task.getApplication(), balancedInstanceName, new CommandMessage(CommandType.INVOKE.getValue(), task.getKey()));
				}).start();
			}
			resetNextDate(currentDatetime, cronExpression);
		}
	}

	private void resetNextDate(String currentDatetime, String cronExpression) {
		NEXT_DATE_CRON_EXPRESSION_MAP.remove(cronExpression);
		prepareNextDate(cronExpression);
	}
}
