package org.andwellness.xml;

import nu.xom.Node;
import nu.xom.Nodes;


/**
 * A collection of helper methods for validating number-based prompt types.
 * 
 * @author selsky
 */
public abstract class AbstractNumberPromptTypeValidator extends AbstractPromptTypeValidator {
	protected int _min;
	protected int _max;
	
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
		
		performExtendedConfigValidation(promptNode, minVNodes, maxVNodes);
	} 
	
	protected int getValidNegOrPosInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value);
		}
	}
	
	protected int getValidNonNegativeInteger(String value) {
		int i = 0;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value);
		}
		
		if(i < 0) {
			throw new IllegalArgumentException("value must be non-negative: " + value);
		}
		
		return i; 
	}
	
	protected int getValidPositiveInteger(String value) {
		int i = 0;
		try {
			i = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("not a valid integer: " + value);
		}
		
		if(i < 1) {
			throw new IllegalArgumentException("value must be positive: " + value);
		}
		
		return i; 
	}
	
	protected abstract void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes);
}
