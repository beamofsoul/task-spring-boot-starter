package com.beamofsoul.cloud.execute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.beamofsoul.cloud.annotation.ConditionalOnAnnotation;
import com.beamofsoul.cloud.annotation.EnableTaskingServer;

@ConditionalOnAnnotation(EnableTaskingServer.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class InvokingTaskRunner implements CommandLineRunner {
	
	@Autowired
	private LoopInvoker timer;
	
	@Override
	public void run(String... args) throws Exception {
		new Thread(() -> timer.loop()).start();
	}
}
