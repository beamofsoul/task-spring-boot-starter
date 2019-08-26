package com.beamofsoul.cloud.socket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingServer;

@ConditionalOnAnnotation(EnableTaskingServer.class)
@Configuration
public class WebSocketConfiguration extends Configurator {

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}
}
