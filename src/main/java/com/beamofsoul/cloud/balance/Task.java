package com.beamofsoul.cloud.balance;

public class Task {

	private String application;
	private String key;

	public Task(String application, String key) {
		super();
		this.application = application;
		this.key = key;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	@Override
	public boolean equals(Object obj) {
		Task otherTask = (Task) obj;
		if (this.application != null && otherTask.getApplication() != null && !this.application.equals(otherTask.getApplication()))
			return false;
		if (this.key != null && otherTask.getKey() != null && !this.key.equals(otherTask.getKey()))
			return false;
		return true;
	}
}
