package org.andwellness.xml;

import java.util.HashSet;
import java.util.Set;

import nu.xom.Node;
import nu.xom.Nodes;

/**
 * Validator for multi_choice_custom and single_choice_custom prompt types.
 * 
 * @author selsky
 */
public class CustomChoicePromptTypeValidator extends ChoicePromptTypeValidator {
	
	/**
	 * Zero properties are allowed in custom choice prompt types, but if properties are configured, they are checked here.
	 */
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);

		// Check to see whether there are any properties to validate
		Nodes propertiesNodes = promptNode.query("properties");
		
		if(propertiesNodes.size() > 0) {
		
			// All k must be number
			// 'v' nodes exist at this point because of schema validation
			
			Nodes kNodes = promptNode.query("properties/p/k"); // could check for the number of 'p' nodes here, but 
			                                                   // the number of 'k' nodes == the number of 'p' nodes
			                                                   // and the values of the 'k' nodes are what needs to be validated 
			int kSize = kNodes.size();
			for(int j = 0; j < kSize; j++) {
				_choices.add(getValidNonNegativeInteger(kNodes.get(j).getValue()));
			}
			
			// Make sure there are not duplicate keys
			Set<Integer> integerSet = new HashSet<Integer>();
			for(Integer i : _choices) {
				if(! integerSet.add(i)) {
					throw new IllegalArgumentException("duplicate found for choice: " + i);
				}
			}
			
			Nodes vNodes = promptNode.query("properties/p/v");
					
			// Make sure there are not duplicate values
			Set<String> valueSet = new HashSet<String>();
			int vSize = vNodes.size();
			
			for(int i = 0; i < vSize; i++) {
				if(! valueSet.add(vNodes.get(i).getValue())) {
					throw new IllegalArgumentException("duplicate found for value: " + vNodes.get(i).getValue());
				}
			}
		}
	}
}
