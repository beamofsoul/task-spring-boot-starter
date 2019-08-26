package com.beamofsoul.cloud.register;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterInformation {

	private static final Map<String, List<String>> REGISTER_INFORMATION_MAP = new HashMap<>();
	
	public static void putAll(Map<String, List<String>> map) {
		REGISTER_INFORMATION_MAP.putAll(map);
	}
	
	public static Map<String, List<String>> get() {
		return REGISTER_INFORMATION_MAP;
	}
}
