package org.andwellness.config.grammar.custom;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.andwellness.config.grammar.parser.ConditionParser;
import org.andwellness.config.grammar.parser.ParseException;
import org.andwellness.config.grammar.syntaxtree.start;

/**
 * A validator for conditions that relies on classes generated from JavaCC and JTB to parse and retrieve data from condition
 * sentences.  
 * 
 * @author selsky
 */
public final class ConditionValidator {
	private static boolean _first = true;
	
	/**
	 * Prevent instantiation.
	 */
	private ConditionValidator() {
		
	}
	
	/**
	 * For command line use for testing. Provide the condition to be validated as the first argument. The condition must be a
	 * double-quoted string.
	 */
	public static void main(String args[]) throws IOException, ConditionParseException {
		Map<String, List<ConditionValuePair>> map = validate(args[0]);
		System.out.println(map);
	}
	
	/**
	 * Validates the provided condition sentence.
	 * 
	 * @param conditionSentence
	 * @return Map of id-value list pairs for each id-operation-value in the provided sentence
	 * @throws ConditionParseException if the sentence does not conform to our grammar (see spec/condition-grammar.jj) 
	 */
	public static Map<String, List<ConditionValuePair>> validate(String conditionSentence) {
		start s = null;
		
		try {
			
			// TODO - fix the ConditionParser instatiation logic by rebuilding the parser and setting the JavaCC parameter STATIC
			// to false. Without the false setting, the weird ReInit logic below must occur.
			
			if(_first) {
				
				_first = false;
				s = new ConditionParser(new StringReader(conditionSentence)).start();
				
			} else {
				ConditionParser.ReInit(new StringReader(conditionSentence)); // ReInit must be called for a multiple parse scenario
				                                                             // i.e., when this method is called from a loop
				s = ConditionParser.start();
			}
			
			ConditionDepthFirst<Map<String, List<ConditionValuePair>>> visitor 
				= new ConditionDepthFirst<Map<String, List<ConditionValuePair>>>();
			Map<String, List<ConditionValuePair>>map = new HashMap<String, List<ConditionValuePair>>(); 
			visitor.visit(s, map);
			return map;
			
		} catch (ParseException pe) {
			
			throw new ConditionParseException("Condition parse failed for condition sentence: " + conditionSentence, pe);
		}
	}
}
