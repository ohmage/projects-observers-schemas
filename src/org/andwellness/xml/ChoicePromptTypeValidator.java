package org.andwellness.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nu.xom.Node;
import nu.xom.Nodes;

import org.andwellness.grammar.custom.ConditionValuePair;

/**
 * Single and multi-choice (custom and non-custom) prompt type validator. 
 * 
 * @author selsky
 */
public class ChoicePromptTypeValidator extends AbstractNumberPromptTypeValidator {
	protected Map<Integer, String> _choices;
	
	public ChoicePromptTypeValidator() {
		_choices = new HashMap<Integer, String>();
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
			throw new IllegalStateException("At least 2 'p' nodes are required for prompt:\n" + promptNode.toXML());
		}
		
		int kSize = kNodes.size();
		for(int j = 0; j < kSize; j++) {
			int key = getValidNonNegativeInteger(kNodes.get(j).getValue());
			if(_choices.containsKey(key)) {
				throw new IllegalArgumentException("duplicate found for choice key: " + key);
			}
			
			_choices.put(getValidNonNegativeInteger(kNodes.get(j).getValue()), null);
		}
		
//		// Make sure there are not duplicate keys
//		Set<Integer> integerSet = new HashSet<Integer>();
//		for(Integer i : _choices) {
//			if(! integerSet.add(i)) {
//				
//			}
//		}
		
		Nodes vNodes = promptNode.query("properties/p/v");
				
		// Make sure there are not duplicate values 
		
		// TODO should duplicate values be allowed in certain cases e.g., when the values are empty strings?
		
		Set<String> valueSet = new HashSet<String>();
		int vSize = vNodes.size();
		
		for(int i = 0; i < vSize; i++) {
			if(! valueSet.add(vNodes.get(i).getValue())) {
				throw new IllegalArgumentException("duplicate found for value: " + vNodes.get(i).getValue());
			}
			_choices.put(Integer.parseInt(kNodes.get(i).getValue()), vNodes.get(i).getValue());
		}
	}

	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			if(! _choices.containsValue(pair.getValue())) {
				throw new IllegalArgumentException("value not found in set of choices: " + pair.getValue());
			} 
		}
		
		// the only conditions allowed are == and !=
		String condition = pair.getCondition();
		
		if(! "==".equals(condition) && ! "!=".equals(condition)) {
			throw new IllegalArgumentException("invalid condition in multi or single choice prompt: " + pair.getCondition());
		}
	}
	
	/**
	 * Checks whether the provided value exists in one of the configured choices.
	 */
	public void checkDefaultValue(String value) {
		if(! _choices.containsValue(value)) {
			throw new IllegalArgumentException("default value [" + value + "] is missing from choices");
		}
	}
	
	protected void performExtendedConfigValidation(Node promptNode, Nodes minVNodes, Nodes maxVNodes) {
		// do nothing
	}
}
