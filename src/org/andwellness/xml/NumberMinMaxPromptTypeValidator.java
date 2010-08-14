package org.andwellness.xml;

import nu.xom.Node;
import nu.xom.Nodes;

/**
 * Validates the prompt types number and hours_before now, which both require a min and max property that must be a valid integer.
 * 
 * @author selsky
 */
public class NumberMinMaxPromptTypeValidator extends AbstractNumberPromptTypeValidator {
	private int _min;
	private int _max;
	
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
		
		_min = getValidNegOrPosInteger(minVNodes.get(0).getValue()); 
		_max = getValidNegOrPosInteger(maxVNodes.get(0).getValue());
		
		if(_max < _min) {
			throw new IllegalStateException("max cannot be greater than min: " + promptNode.toXML());
		}
	}

	/**
	 * Checks values (from, e.g., conditions) against the min and max defined by this instance. 
	 */
	@Override
	public void validateValue(String value) {
		if(! isSkipped(value)) {
			int v = 0;
			try {
				v = Integer.parseInt(value);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("not a number: " + value); 
			}
			if(v < _min || v > _max) {
				throw new IllegalArgumentException("number or hours_before_now prompt value of out range. min=" + _min + ", max=" +
				    _max + ", value=" + value);
			}
		}
	}
}
