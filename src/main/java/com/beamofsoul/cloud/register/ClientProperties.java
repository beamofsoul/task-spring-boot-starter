package com.beamofsoul.cloud.register;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingClient;

@ConditionalOnAnnotation(EnableTaskingClient.class)
@ConfigurationProperties("task.client")
@Component
public class ClientProperties {

	private String instanceName;
	private List<String> servers;

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public List<String> getServers() {
		return servers;
	}

	public void setServers(List<String> servers) {
		this.servers = servers;
	}
}
