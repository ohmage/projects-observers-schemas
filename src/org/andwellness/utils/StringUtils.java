package org.andwellness.utils;

import java.util.regex.Pattern;

/**
 * A utility class for analyzing String values.
 * 
 * @author John Jenkins
 */
public class StringUtils {
	private static Pattern _urnPattern = Pattern.compile("[a-z0-9_]+");
	
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
	
	/**
	 * Validates that a URN is valid.
	 * 
	 * @return Returns true if the provided value is a valid URN; otherwise, it
	 * 		   returns false.
	 */
	public static boolean isValidUrn(String value) {
		if(isEmptyOrWhitespaceOnly(value)) {
			return false;
		}
		
		String s = value.toLowerCase();
		
		if(! s.startsWith("urn:")) {
			return false;
		}
		
		if(s.length() < 7) { // disallow anything shorter than urn:a:a 
			return false;
		}
		
		String[] a = s.split(":");
		
		if(a.length < 3) { // require at least three colon-delimited sections
			return false;
		}
		
		for(int i = 1; i < a.length; i++) { // each section after the initial urn must match _urnPattern
			if(! _urnPattern.matcher(a[i]).matches()) {
				return false;
			}
		}
		
		return true;
	}
}
