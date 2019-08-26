package com.beamofsoul.cloud.balance;

public class ClientInstance {

	private final String instanceName;
	private final int initializedWeight;
	private int currentWeight;

	public ClientInstance(String instanceName, int weight) {
		this.instanceName = instanceName;
		this.initializedWeight = weight;
		this.currentWeight = weight;
	}

	public int getCurrentWeight() {
		return currentWeight;
	}

	public int getWeight() {
		return initializedWeight;
	}

	public void setCurrentWeight(int currentWeight) {
		this.currentWeight = currentWeight;
	}

	public String getInstanceName() {
		return instanceName;
	}
}
