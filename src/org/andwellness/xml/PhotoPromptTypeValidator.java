package org.andwellness.xml;

import nu.xom.Node;
import nu.xom.Nodes;

import org.andwellness.grammar.custom.ConditionValuePair;

/**
 * Validates the photo prompt type.
 * 
 * @author selsky
 */
public class PhotoPromptTypeValidator extends AbstractNumberPromptTypeValidator {
	
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
		if(1 != propertyNodes.size()) {
			throw new IllegalStateException("invalid prompt configuration: " + promptNode.toXML());
		}
		
		Nodes resNodes = promptNode.query("properties/p/k[text()='res']");
		if(1 != resNodes.size()) {
			throw new IllegalStateException("missing 'res' property for XML fragment: " + promptNode.toXML());
		}
						
		Nodes resVNodes = resNodes.get(0).getParent().query("v"); // the schema check should prevent this 
		if(1 != resVNodes.size()) {
			throw new IllegalStateException("missing or extra 'res' value for XML fragment: " + promptNode.toXML());
		}
		
		getValidPositiveInteger(resVNodes.get(0).getValue()); // TODO we need a valid set of values for res (720, etc)
	}

	/**
	 * Checks values (from, e.g., conditions) against the min and max defined by this instance. 
	 */
	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			throw new IllegalArgumentException("invalid value for photo prompt type: " + pair.getValue());
		}
	}
	
	protected void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes) {
		// do nothing
	}
}
