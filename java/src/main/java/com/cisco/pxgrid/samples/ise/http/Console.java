package com.cisco.pxgrid.samples.ise.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Console {
	public static void log(String message) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getDefault());
		String date = formatter.format(new Date());
		
		String prepend = date + " [" + Thread.currentThread().getName() 
							+ "-" + Thread.currentThread().getId() + "]: "; 
		System.out.println(prepend + message);
	}
}
