package org.andwellness.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for PromptTypeValidators.
 * 
 * @author selsky
 */
public class PromptTypeValidatorFactory {
	private static final List<String> _promptTypes;
	
	static {
		_promptTypes = new ArrayList<String>();
		
		_promptTypes.add("timestamp");
		_promptTypes.add("number");
		_promptTypes.add("hours_before_now");
		_promptTypes.add("text");
		_promptTypes.add("multi_choice");
		_promptTypes.add("multi_choice_custom");
		_promptTypes.add("single_choice");
		_promptTypes.add("single_choice_custom");
		_promptTypes.add("photo");
	}
	
	// prevent instantiation
	private PromptTypeValidatorFactory() {
		
	}
	
	/**
	 * Returns a new PromptTypeValidator for the provided promptType. 
	 */
	public static PromptTypeValidator getValidator(String promptType) {
		
		if(null == promptType) {
			throw new IllegalArgumentException("cannot create a PromptTypeValidator for a missing prompt type.");
		}
		
		if("number".equals(promptType) || "hours_before_now".equals(promptType)) {
			
			return new NumberMinMaxPromptTypeValidator();
			
			
		} else if ("single_choice".equals(promptType) || "multi_choice".equals(promptType)
				   || "single_choice_custom".equals(promptType) || "multi_choice_custom".equals(promptType)) {
			
			// for condition validation the custom types are treated the same way as the non-custom
			
			return new SingleAndMultiChoicePromptTypeValidator();
			
		} else if("text".equals("promptType"))  {
		
			return new TextMinMaxPromptTypeValidator();
		
		} else if("photo".equals("promptType"))  {
		
			return new PhotoPromptTypeValidator();
			
		} else { 
			
			throw new IllegalArgumentException("unknown prompt type.");
		}
	}
	
	/**
	 * Returns whether the provided promptType is supported by this factory. 
	 */
	public static boolean isValidPromptType(String promptType) {
		return _promptTypes.contains(promptType);
	}                                                           
}
