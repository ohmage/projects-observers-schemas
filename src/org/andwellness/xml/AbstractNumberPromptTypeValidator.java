package org.andwellness.xml;


/**
 * A collection of helper methods for validating number-based prompt types.
 * 
 * @author selsky
 */
public abstract class AbstractNumberPromptTypeValidator extends AbstractPromptTypeValidator {

	protected int getValidNegOrPosInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value);
		}
	}
	
	protected int getValidPosInteger(String value) {
		int i = 0;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value);
		}
		
		if(i < 0) {
			throw new IllegalArgumentException("value must be positive: " + value);
		}
		
		return i; 
	}	
}
