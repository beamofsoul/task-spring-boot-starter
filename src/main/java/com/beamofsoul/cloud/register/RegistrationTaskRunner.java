package com.beamofsoul.cloud.register;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingClient;

@ConditionalOnAnnotation(EnableTaskingClient.class)
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegistrationTaskRunner implements CommandLineRunner {
	
	@Autowired
	private ClientProperties clientProperties;
	
	@Autowired
	private TextWebSocketHandler webSocketHandler;

	/**   
	 * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])   
	 */
	@Override
	public void run(String... args) throws Exception {
		register();
	}
	
	private void register() {
		List<String> servers = clientProperties.getServers();
		for (String hostPort : servers) {
			String url = "ws://" + hostPort + "/websocket/" + clientProperties.getInstanceName() + "/" + UUID.randomUUID();
			StandardWebSocketClient client = new StandardWebSocketClient();
			WebSocketConnectionManager manager = new WebSocketConnectionManager(client, webSocketHandler, url);
			manager.start();
		}
	}
}
