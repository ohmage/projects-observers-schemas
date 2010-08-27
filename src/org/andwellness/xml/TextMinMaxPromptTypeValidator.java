package org.andwellness.xml;

import nu.xom.Node;
import nu.xom.Nodes;

import org.andwellness.grammar.custom.ConditionValuePair;

/**
 * Validates the text prompt type.
 * 
 * @author selsky
 */
public class TextMinMaxPromptTypeValidator extends AbstractNumberPromptTypeValidator {

	/**
	 * Only allowed value here is SKIPPED.
	 */
	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			throw new IllegalArgumentException("invalid value for photo prompt type: " + pair.getValue());
		}
	}
	
	/**
	 * Makes sure that max is greater than min and that min and max are both positive integers.
	 */
	protected void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes) {
		int min = getValidPositiveInteger(minVNodes.get(0).getValue()); 
		int max = getValidPositiveInteger(maxVNodes.get(0).getValue());
		
		if(max < min) {
			throw new IllegalStateException("max cannot be greater than min: " + promptNode.toXML());
		}
	}
}
