package com.beamofsoul.cloud.register;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beamofsoul.cloud.balance.ClientInstance;
import com.beamofsoul.cloud.balance.SmoothWeightedRoundRobin;
import com.beamofsoul.cloud.balance.Task;

public class RegistedTaskContainer {
	
	public static final Logger LOG = LoggerFactory.getLogger(RegistedTaskContainer.class);
	
	private static final Map<String, Map<Task, SmoothWeightedRoundRobin>> TASK_ROBIN_MAP = new ConcurrentHashMap<>();
	
	public static Set<String> getCronExpressions() {
		return TASK_ROBIN_MAP.keySet();
	}
	
	public static void remove(String application, String instance) {
		Iterator<Entry<String, Map<Task, SmoothWeightedRoundRobin>>> taskRobinMapEntries = TASK_ROBIN_MAP.entrySet().iterator();
		
		while (taskRobinMapEntries.hasNext()) {
			Entry<String, Map<Task, SmoothWeightedRoundRobin>> taskRobinMapEntry = taskRobinMapEntries.next();
			Map<Task, SmoothWeightedRoundRobin> taskRobinMap = taskRobinMapEntry.getValue();
			Iterator<Entry<Task, SmoothWeightedRoundRobin>> taskRobinEntries = taskRobinMap.entrySet().iterator();
			
			while (taskRobinEntries.hasNext()) {
				Entry<Task, SmoothWeightedRoundRobin> taskRobinEntry = taskRobinEntries.next();
				Task task = taskRobinEntry.getKey();
				SmoothWeightedRoundRobin robin = taskRobinEntry.getValue();
				
				if (task.getApplication().equals(application) && robin.getInstanceNames().contains(instance)) {
					robin.remove(instance);
				}
				
				if (robin.isEmpty()) {
					taskRobinEntries.remove();
				}
			}
			
			if (taskRobinMap.isEmpty()) {
				taskRobinMapEntries.remove();
			}
		}
	}

	public static void putAll(Map<String, Map<String, List<String>>> clientTasks) {
		for (Entry<String, Map<String, List<String>>> cronEntry : clientTasks.entrySet()) {
			String cronExpression = cronEntry.getKey();
			Map<String, List<String>> applicationInstanceTaskKeyMap = cronEntry.getValue();
			
			for (Entry<String, List<String>> serviceEntry : applicationInstanceTaskKeyMap.entrySet()) {
				String applicationInstance = serviceEntry.getKey();
				String[] array = applicationInstance.split(":"); // application:instance
				String application = array[0];
				String instance = array[1];
				List<String> taskKeys = serviceEntry.getValue(); // task keys
				
				for (String taskKey : taskKeys) {
					String[] taskArray = taskKey.split(":");
					String pureTaskKey = taskArray[0];
					Integer weight = taskArray.length == 1 ? 1 : Integer.valueOf(taskArray[1]);
					Task currentTask = new Task(application, pureTaskKey);
					
					if (TASK_ROBIN_MAP.containsKey(cronExpression)) {
						ClientInstance balancedInstance = new ClientInstance(instance, weight);
						Map<Task, SmoothWeightedRoundRobin> taskRobin = TASK_ROBIN_MAP.get(cronExpression);
						if (contains(taskRobin, currentTask)) {
							getRobin(taskRobin, currentTask).add(balancedInstance);
						} else {
							taskRobin.put(currentTask, new SmoothWeightedRoundRobin(application, balancedInstance));
						}
					} else {
						ClientInstance balancedInstance = new ClientInstance(instance, weight);
						TASK_ROBIN_MAP.put(cronExpression, new HashMap<Task, SmoothWeightedRoundRobin>() {
							private static final long serialVersionUID = 1L;
						{
							put(currentTask, new SmoothWeightedRoundRobin(application, balancedInstance));
						}});
					}
				}
			}
		}
		formatedPrint();
	}

	public static Map<Task, SmoothWeightedRoundRobin> get(String cronExpression) {
		return TASK_ROBIN_MAP.get(cronExpression);
	}
	
	private static boolean contains(Map<Task, SmoothWeightedRoundRobin> taskRobin, Task currentTask) {
		return taskRobin.keySet().stream().filter(e -> e.equals(currentTask)).count() > 0;
	}
	
	private static SmoothWeightedRoundRobin getRobin(Map<Task, SmoothWeightedRoundRobin> taskRobin, Task currentTask) {
		Optional<Task> optionalTask = taskRobin.keySet().stream().filter(e -> e.equals(currentTask)).distinct().findFirst();
		return taskRobin.get(optionalTask.orElse(null));
	}
	
	public static void formatedPrint() {
		Set<Entry<String, Map<Task, SmoothWeightedRoundRobin>>> entrySet = TASK_ROBIN_MAP.entrySet();
		LOG.debug("---------------------------------------------------------------------------");
		for (Entry<String, Map<Task, SmoothWeightedRoundRobin>> entry : entrySet) {
			LOG.debug("#### CronExpression: " + entry.getKey());
			Map<Task, SmoothWeightedRoundRobin> taskRobinMap = entry.getValue();
			for (Entry<Task, SmoothWeightedRoundRobin> taskRobinEntry : taskRobinMap.entrySet()) {
				Task task = taskRobinEntry.getKey();
				SmoothWeightedRoundRobin robin = taskRobinEntry.getValue();
				List<String> instanceNames = robin.getInstanceNames();
				for (String instance : instanceNames) {
					LOG.debug(task.getApplication() + ":" + instance + " ===> " + task.getKey());
				}
			}
			if (entrySet.size() > 1) {
				LOG.debug("");
			}
		}
		LOG.debug("---------------------------------------------------------------------------");
	}
}
