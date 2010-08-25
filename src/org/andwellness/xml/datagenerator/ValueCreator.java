package org.andwellness.xml.datagenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Utility class for creating various values for the generic data point format.
 * 
 * @author selsky
 */
public class ValueCreator {
	private static Random _random = new Random(); 
	private static String[] _tzs = {"EST", "CST", "MST", "PST"}; // American timezone hegemony ;)
	private static SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private ValueCreator() { };
	
	public static double latitude() {
		return _random.nextInt() % 90 + _random.nextDouble();
	}
	
	public static double longitude() {
		return _random.nextInt() % 180 + _random.nextDouble();
	}
	
	public static long epoch() {
		return System.currentTimeMillis();
	}

	public static long epoch(int num_days_ago) {
		return System.currentTimeMillis() - 86400000l * num_days_ago; 
	}
	
	public static String tz() {
		return _tzs[Math.abs(_random.nextInt()) % (_tzs.length)];
	}
	
	public static String date() {
// use the local timezone for now
// if specific timezones are added in the future, the epoch method will have to change so the value returned has the same tz
//		sdf.setTimeZone(TimeZone.getTimeZone(tz));
		return isoDateFormat.format(new Date());
	}
	
	public static String date(Date date) {
	    return isoDateFormat.format(date);
	}

	public static String date(int num_days_ago) {
// use the local timezone for now
// if specific timezones are added in the future, the epoch method will have to change so the value returned has the same tz
//		sdf.setTimeZone(TimeZone.getTimeZone(tz));
		return isoDateFormat.format(new Date(epoch(num_days_ago)));
	}
	
	public static String hours_before_date(Date date, int hoursBeforeDate) {
	    // Create a Date that is the passed Date minus the passed number of hours
	    Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // Subtract the hours from the time
        calendar.add(Calendar.HOUR_OF_DAY, -1 * hoursBeforeDate);
        return ValueCreator.date(calendar.getTime());
	}
	
	// Return a date that corresponds to the hours_before_now prompt type
	// The properties conforms to min:number, max:number
	public static String hours_before_now(Map<String, String> properties) {
	    int minHours = Integer.valueOf(properties.get("min")).intValue();
	    int maxHours = Integer.valueOf(properties.get("max")).intValue();
	    int range = maxHours - minHours;
	    int hours = _random.nextInt() % range + minHours;
	    
	    // Subtract hours from the current time and create a new Date
	    Date newDate = new Date(epoch() - hours * 1000 * 60 * 60);
	    
	    return isoDateFormat.format(newDate);
	}
	
	// Return a random key from the list of key/value pairs
	public static String single_choice(Map<String, String> properties) {
	    Set<String> keySet = properties.keySet();
	    // Randomly select a key to return
	    int keyChoice = _random.nextInt(keySet.size());
	    // Grab the key
	    String chosenKey = (String) keySet.toArray()[keyChoice];
	    
	    return chosenKey;
	}
	
	// Return a list of chosen keys
	public static List<String> multiChoice(Map<String, String> properties) {
	    List<String> chosenChoices = new ArrayList<String>();
	    
	    Set<String> keySet = properties.keySet();
	    
	    // Fifty percent chance of selecting each choice
	    double oddsOfChoosing = 0.5;
	    
	    // For each key in the Map, randomly decide whether or not to choose it
	    Iterator<String> itr = keySet.iterator();
	    while(itr.hasNext()) {
	        String keyValue = itr.next();
	        
	        if (_random.nextDouble() < oddsOfChoosing) {
	            chosenChoices.add(keyValue);
	        }
	    }
	    
	    return chosenChoices;
	}
	
	public static double randomPositiveDouble() {
		return Math.abs(_random.nextDouble());
	}
	
	public static boolean randomBoolean() {
		return _random.nextBoolean(); 
	}
	
	public static String randomTime() {
		String hours =  String.valueOf(Math.abs(_random.nextInt() % 24));
		
		int m = Math.abs(_random.nextInt() % 60);
		String minutes = m < 10 ? "0" + m : String.valueOf(m);
		
		return hours + ":" + minutes; 
	}
	
	// Can pass an hour around which the returned times will be centered
	// The data will be distributed uniformly over the range
	public static String randomTime(int average_hour, int hour_range) {
	    int h = Math.abs((average_hour + _random.nextInt(hour_range+1) - hour_range / 2) % 24);
	    String hours = String.valueOf(h);
	    
	    int m = Math.abs(_random.nextInt() % 60);
        String minutes = m < 10 ? "0" + m : String.valueOf(m);
	    
	    return hours + ":" + minutes;
	}
	
	public static int randomPositiveIntModulus(int modulus) {
		return Math.abs(_random.nextInt() % modulus);
	}
}
