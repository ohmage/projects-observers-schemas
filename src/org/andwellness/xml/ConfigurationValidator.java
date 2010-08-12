package org.andwellness.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.grammar.custom.ConditionValidator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The main driver for the configuration validation process.
 * 
 * @author selsky
 */
public class ConfigurationValidator {
	private static final String _schemaFile = "spec/configuration.xsd";
	private static final List<String> _promptTypes = new ArrayList<String>();
	
	static {
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
	
	/**
	 * args[0]: the file name of the file to validate
	 */
	public static void main(String[] args) throws IOException, SAXException, ParsingException, ValidityException {
		
		// 1. Validate against schema: spec/configuration.xsd
		// 2. Make sure all ids are unique
		// 3. All prompts must be of a known type
		// 4. Conditions
		// 4a. The first prompt in a contentList cannot have a condition.
		// 4b. Conditions must validate against the condition grammar.
		// 4c. ids in conditions must exist in a prompt previous to the current prompt
		// 4d. The values in conditions must be allowable for the prompt type indicated by the id on the left hand side of the 
		// operation in each condition
		// 5. Prompt type validation - configuration properties must be valid for the type.
		
		if(args.length < 1) {
			throw new IllegalArgumentException("You must pass a file name as the first argument.");
		}
		
		String fileName = args[0];
		
		// --
		// Validate instance document against the schema
		// --
		
		StreamSource schemaDocument = new StreamSource(new File(_schemaFile));
		SAXSource instanceDocument = new SAXSource(new InputSource(new FileInputStream(fileName)));
		
		// Originally attempted to use "http://www.w3.org/XML/XMLSchema/v1.1" here, but neither Xerces2 nor the native
		// Java 6 implementation supports it out of the box.  
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);  
		// System.out.println(sf.getClass().getName());
		
		Schema s = sf.newSchema(schemaDocument);
		Validator v = s.newValidator();
		v.validate(instanceDocument);
		
		System.out.println("Successful schema validation of " + args[0] + " against schema " + _schemaFile);
		System.out.println("Beginning configuration validation");
		
		// At this point the instance document is valid. Here a re-parse of the instance document is performed in
		// order to get it into XOM which has a very nice XPath API
		
		Builder builder = new Builder();
		Document document = builder.build(fileName);
		// System.out.println(document.toXML());
		
		Element root = document.getRootElement();
		
		// Make sure all ids are unique
		
		Nodes idNodes = root.query("//id"); // find all of the id elements in the file
		int size = idNodes.size();
		List<String> idList = nodesToValueList(idNodes);
		Set<String> stringSet = new HashSet<String>(size);
		
		for(int i = 0; i < size; i++) {
			String value = idNodes.get(i).getValue();
			if(! stringSet.add(value)) { // if add() returns false, it means there is a duplicate in the list
				
				throw new IllegalStateException("Invalid configuration: a duplicate id was found: " + value);
				
			}
		}
		
		System.out.println("no duplicate ids found");
		
		// -- 
		// Make sure all prompts are of a known type
		// --
		
		Nodes promptTypeNodes = root.query("//promptType");
		List<String> stringList = nodesToValueList(promptTypeNodes);
		
		for(String string : stringList) {
			
			if(! _promptTypes.contains(string)) {
			
				throw new IllegalStateException("Invalid configuration: an unknown prompt type was found: " + string);
			}
		}
		
		System.out.println("no unknown prompt types found");
		
		// --
		// Validate all conditions
		// --
		
		Nodes conditionNodes = root.query("//condition");
		List<String> conditionList = nodesToValueList(conditionNodes);
		int conditionIndex;	
		
		for(String condition : conditionList) {
			
			if("".equals(condition)) {
				continue;
			}
			
			System.out.println("validating condition: " + condition);
			
			Map<String, List<String>> idValuesMap = ConditionValidator.validate(condition);
			
			// ok, the condition is syntactically valid. now check its sanity. 
			
			// The ids in this condition must be for ids in the current survey (ignore the survey id) that exist 
			// previously to the prompt containing the current condition
			
			
			
		}
		
		
		
		
		System.out.println("configuration validation successful");
		
	}

	/**
	 * Converts the provided XOM Nodes to a List of values contained in each Node in the Nodes. The XOM API is outdated and 
	 * its Nodes class is not a java.util.List (or some other standard collection).
	 * 
	 * This method can only be used if the Nodes represent Text nodes (instead of say, elements with child elements).
	 */
	private static List<String> nodesToValueList(Nodes nodes) {
		if(null == nodes) {
			return null; // should be an empty immutable list
		}
		
		int size = nodes.size();
		List<String> stringList = new ArrayList<String>();
		
		for(int i = 0; i < size; i++) {
			stringList.add(nodes.get(i).getValue());
		}
		
		return stringList;
	}
}
 