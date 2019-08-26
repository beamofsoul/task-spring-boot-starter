package com.beamofsoul.cloud.balance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class SmoothWeightedRoundRobin {
	
	private String applicationName = null;
	private volatile List<ClientInstance> instanceList = new ArrayList<>();
	private ReentrantLock lock = new ReentrantLock();
 
	public SmoothWeightedRoundRobin(String applicationName, ClientInstance ...instances) {
	 	this.applicationName = applicationName;
	 	instanceList.addAll(Arrays.asList(instances));
	}

	public ClientInstance select(){
		try {
			lock.lock();
			return this.select0() ;
		}finally {
			lock.unlock();
		}
	}

	private ClientInstance select0() {
		int totalWeight = 0;
		ClientInstance maxInstance = null;
		int maxWeight = 0;

		for (int i = 0; i < instanceList.size(); i++) {
			ClientInstance n = instanceList.get(i);
			totalWeight += n.getWeight();
			n.setCurrentWeight(n.getCurrentWeight() + n.getWeight());

			if (maxInstance == null || maxWeight < n.getCurrentWeight() ) {
			 maxInstance = n;
			 maxWeight = n.getCurrentWeight();
			}
		}
		maxInstance.setCurrentWeight(maxInstance.getCurrentWeight() - totalWeight);
		return maxInstance;
	}
 
	public void remove(String instanceName) {
		Iterator<ClientInstance> iterator = instanceList.iterator();
		while(iterator.hasNext()) {
			if (iterator.next().getInstanceName().equals(instanceName)) {
				iterator.remove();
				break;
			}
		}
	}
	
	public int size() {
		return instanceList.size();
	}
	
	public boolean isEmpty() {
		return instanceList.isEmpty();
	}
	
	public void add(ClientInstance instance) {
		instanceList.add(instance);
	}
	
	public String getApplicationName() {
		return this.applicationName;
	}
	
	public List<String> getInstanceNames() {
		return instanceList.stream().map(e -> e.getInstanceName()).collect(Collectors.toList());
	}
}
