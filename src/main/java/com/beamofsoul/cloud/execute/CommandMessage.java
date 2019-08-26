package com.beamofsoul.cloud.execute;

import java.util.HashMap;

import com.alibaba.fastjson.JSONObject;

public class CommandMessage{
	
	private String command;
	private String message;
	
	public CommandMessage() {
		
	}
	
	public static CommandMessage parse(String jsonString) {
		return JSONObject.parseObject(jsonString, CommandMessage.class);
	}
	
	public CommandMessage(String command, String message) {
		this.command = command;
		this.message = message;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public static enum CommandType {
		REGISTER("register"), // 客户端向服务端注册的指令
		INVOKE("invoke"); // 客户端执行目标任务的指令
		
		private static HashMap<String, CommandType> codeValueMap = new HashMap<>(3);
		private String value;
		
		static {
			for (CommandType commandType : CommandType.values()) {
				codeValueMap.put(commandType.value, commandType);
			}
		}

		CommandType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		public static CommandType getInstance(String code) {
			return codeValueMap.get(code);
		}
		
		public static boolean exists(String code) {
			return codeValueMap.containsKey(code);
		}
	}
}
