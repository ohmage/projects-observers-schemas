package org.andwellness.config.grammar.custom;

/**
 * Associates a condition (an operator value) with a value.
 * 
 * @author selsky
 */
public class ConditionValuePair {
	private String _condition;
	private String _value;
	
	public String getCondition() {
		return _condition;
	}
	
	public void setCondition(String condition) {
		_condition = condition;
	}
	
	public String getValue() {
		return _value;
	}
	
	public void setValue(String value) {
		_value = value;
	}
	
	@Override
	public String toString() {
		return "ConditionValuePair [_condition=" + _condition + ", _value="
				+ _value + "]";
	}
}
