package org.andwellness.xml;

import nu.xom.Node;

import org.andwellness.grammar.custom.ConditionValuePair;

/**
 * Prompt types have collections of properties that (1) have unique requirements and (2) place bounds on values that are allowed in
 * conditions. 
 * 
 * @author selsky
 */
public interface PromptTypeValidator {
	
	/**
	 * Validates the properties bundle for a specific instance of a type. Also sets the properties so they can be used for 
	 * validation of values of the type.
	 */
	public void validateAndSetConfiguration(Node promptNode);
	
	/**
	 * Determine if the provided value (most likely from a condition statement) is valid for the type instance.
	 */
	public void validateConditionValuePair(ConditionValuePair pair);
}
