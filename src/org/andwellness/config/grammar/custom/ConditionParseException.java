package org.andwellness.config.grammar.custom;

import org.andwellness.config.grammar.parser.ParseException;

/**
 * Wraps the JavaCC ParseException in a more friendly container (RuntimeException instead of the standard Exception).
 * 
 * @author selsky
 */
@SuppressWarnings("serial")
public class ConditionParseException extends RuntimeException {
	
	public ConditionParseException(String message, ParseException cause) {
		super(message, cause);
	}

}
