package com.beamofsoul.cloud.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CronDateUtils {
	
	private static SimpleDateFormat formatter;
	static {
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public static String getCurrentDatetime() {
		return getCurrentDatetime(new Date());
	}
	
	public static String getCurrentDatetime(Date date) {
		return formatter.format(date);
	}

	public static String getNextInvokeDatetime(String cronExpression) { 
		if (cronExpression == null || cronExpression.length() < 1) { 
			return null; 
		} else { 
			CronExpression exp = null;
			try {
				exp = new CronExpression(cronExpression);
				return formatter.format(exp.getNextValidTimeAfter(new Date()));
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			} 
		} 
	}
}
