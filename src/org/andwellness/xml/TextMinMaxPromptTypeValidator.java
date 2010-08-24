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
	 * Checks that the min and max properties exist and that their values are valid.
	 * 
	 * @throws IllegalArgumentException if the configuration is invalid.
	 */
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);
		
		// make sure there are no unknown props
		Nodes propertyNodes = promptNode.query("properties/p"); 
		if(2 != propertyNodes.size()) {
			throw new IllegalStateException("invalid prompt configuration: " + promptNode.toXML());
		}
		
		Nodes minNodes = promptNode.query("properties/p/k[text()='min']");  
		if(1 != minNodes.size()) {
			throw new IllegalStateException("missing or extra 'min' property for XML fragment: " + promptNode.toXML());
		}
		
		Nodes maxNodes = promptNode.query("properties/p/k[text()='max']");
		if(1 != maxNodes.size()) {
			throw new IllegalStateException("missing or extra 'max' property for XML fragment: " + promptNode.toXML());
		}
		
		Nodes minVNodes = minNodes.get(0).getParent().query("v");
		if(1 != minVNodes.size()) {
			throw new IllegalStateException("missing or extra 'min' value for XML fragment: " + promptNode.toXML());
		}
		
		Nodes maxVNodes = maxNodes.get(0).getParent().query("v");
		if(1 != maxVNodes.size()) {
			throw new IllegalStateException("missing or extra 'max' value for XML fragment: " + promptNode.toXML());
		}
		
		int min = getValidPosInteger(minVNodes.get(0).getValue()); 
		int max = getValidPosInteger(maxVNodes.get(0).getValue());
		
		if(max < min) {
			throw new IllegalStateException("max cannot be greater than min: " + promptNode.toXML());
		}
	}

	/**
	 * Only allowed value here is SKIPPED.
	 */
	@Override
	public void validateValue(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			throw new IllegalArgumentException("invalid value for photo prompt type: " + pair.getValue());
		}
	}
}
