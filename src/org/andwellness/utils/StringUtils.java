package org.andwellness.utils;

/**
 * A utility class for analyzing String values.
 * 
 * @author John Jenkins
 */
public class StringUtils {
	
	/**
	 * Hidden default constructor as this is a helper class but not one that
	 * should be instantiated.
	 */
	private StringUtils() {
		
	}
	
	/**
	 * Checks for a null or empty (zero-length or all whitespace) String.
	 * 
	 * A method with the same signature and behavior as this one exists in the
	 * MySQL JDBC code. Taken from the server code.
	 * 
	 * @return true if the String is null, empty, or all whitespace
	 *         false otherwise
	 */
	public static boolean isEmptyOrWhitespaceOnly(String string) {
		
		return null == string || "".equals(string.trim()); 
		
	}
}
