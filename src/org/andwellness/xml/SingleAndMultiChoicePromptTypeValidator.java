package org.andwellness.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.Node;
import nu.xom.Nodes;

/**
 * Single and multi choice (custom and non-custom) prompt type validator. 
 * 
 * @author selsky
 */
public class SingleAndMultiChoicePromptTypeValidator extends AbstractNumberPromptTypeValidator {
	private List<Integer> _choices;
	
	public SingleAndMultiChoicePromptTypeValidator() {
		_choices = new ArrayList<Integer>();
	}
	
	@Override
	public void validateAndSetConfiguration(Node promptNode) {
		setSkippable(promptNode);

		// At least two k-v pairs must be present
		// All k must be number
		// 'v' nodes exist at this point because of schema validation
		
		Nodes kNodes = promptNode.query("properties/p/k"); // could check for the number of 'p' nodes here, but 
		                                                   // the number of 'k' nodes == the number of 'p' nodes
		                                                   // and the values of the 'k' nodes are what needs to be validated 
		if(kNodes.size() < 2) {
			throw new IllegalStateException("At least 2 'p' nodes are required for prompt" + promptNode.toXML());
		}
		
		int kSize = kNodes.size();
		for(int j = 0; j < kSize; j++) {
			_choices.add(getValidPosInteger(kNodes.get(j).getValue()));
		}
		
		// Make sure there are not duplicate keys
		Set<Integer> integerSet = new HashSet<Integer>();
		for(Integer i : _choices) {
			if(! integerSet.add(i)) {
				throw new IllegalArgumentException("duplicate found for choice: " + i);
			}
		}
	}

	@Override
	public void validateValue(String value) {
		if(! isSkipped(value)) {
			int i = getValidPosInteger(value);
			
			if(! _choices.contains(i)) {
				throw new IllegalArgumentException("value not found in set of choices: " + value);
			} 
		}
	}
}
