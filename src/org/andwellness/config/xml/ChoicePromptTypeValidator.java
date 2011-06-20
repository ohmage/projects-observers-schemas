package org.andwellness.config.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import nu.xom.Node;
import nu.xom.Nodes;

import org.andwellness.config.grammar.custom.ConditionValuePair;

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
		
		Nodes kNodes = promptNode.query("properties/property/key"); // could check for the number of 'p' nodes here, but 
		                                                            // the number of 'k' nodes == the number of 'p' nodes
		                                                            // and the values of the 'k' nodes are what needs to be validated 
		if(kNodes.size() < 2) {
			throw new IllegalStateException("At least 2 'property' nodes are required for prompt:\n" + promptNode.toXML());
		}
		
		int kSize = kNodes.size();
		for(int j = 0; j < kSize; j++) {
			int key = getValidNonNegativeInteger(kNodes.get(j).getValue().trim());
			if(_choices.containsKey(key)) {
				throw new IllegalArgumentException("duplicate found for choice key: " + key);
			}
			
			_choices.put(getValidNonNegativeInteger(kNodes.get(j).getValue().trim()), null);
		}
		
//		// Make sure there are not duplicate keys
//		Set<Integer> integerSet = new HashSet<Integer>();
//		for(Integer i : _choices) {
//			if(! integerSet.add(i)) {
//				
//			}
//		}
		
		Nodes lNodes = promptNode.query("properties/property/label");
				
		// Make sure there are not duplicate values 
		
		// TODO should duplicate values be allowed in certain cases e.g., when the values are empty strings?
		
		Set<String> valueSet = new HashSet<String>();
		int vSize = lNodes.size();
		
		for(int i = 0; i < vSize; i++) {
			if(! valueSet.add(lNodes.get(i).getValue().trim())) {
				throw new IllegalArgumentException("duplicate found for label: " + lNodes.get(i).getValue().trim());
			}
			_choices.put(Integer.parseInt(kNodes.get(i).getValue().trim()), lNodes.get(i).getValue().trim());
		}
		
		
		// This is an edge case, but until we have more of them it seems ok here
		
		String promptType = promptNode.query("promptType").get(0).getValue().trim();
		if("single_choice".equals(promptType)) {
			
			String displayType = promptNode.query("displayType").get(0).getValue().trim();
			if("count".equals(displayType) || "measurement".equals(displayType)) {
				
				// An integer value is required for each choice. 
				Nodes vNodes = promptNode.query("properties/property/value");
				if(vNodes.size() < 1) {
					throw new IllegalArgumentException("values are required for single_choice prompts that have a displayType of "
						+ "count or measurement");					
				}
				if(vNodes.size() != kNodes.size()) {
					throw new IllegalArgumentException("values are required for each choice in single_choice prompts that have a " 
						+ "displayType of count or measurement");
				}
				int vNodesSize = vNodes.size();
				for(int i = 0; i < vNodesSize; i++) {
					try {
						Float.parseFloat(vNodes.get(i).getValue().trim());
					} catch(NumberFormatException nfe) {
						throw new IllegalArgumentException("value must be an integer for choice option in prompt " 
							+ promptNode.toXML());
					}
				}
			}
		}
	}

	@Override
	public void validateConditionValuePair(ConditionValuePair pair) {
		if(! isSkipped(pair.getValue())) {
			int i = 0; 
			try {
				i = Integer.parseInt(pair.getValue());
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("invalid condition value: " + i);
			}
			
			if(! _choices.containsKey(i)) {
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
