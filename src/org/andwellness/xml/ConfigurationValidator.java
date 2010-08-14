package org.andwellness.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.grammar.custom.ConditionParseException;
import org.andwellness.grammar.custom.ConditionValidator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The main driver for the configuration validation process. This is a giant procedural class that can be refactored into a nicer
 * OO form once our configuration process coalesces a bit.
 * 
 * @author selsky
 */
public class ConfigurationValidator {
	
	/**
	 * args[0]: the file name of the file to validate
	 */
	public static void main(String[] args) throws IOException, SAXException, ParsingException, ValidityException {
		// Configure log4j. (pointing to System.out)
		BasicConfigurator.configure();
		
		//
		// 1. Validate against schema: spec/configuration.xsd
		// 2. Make sure all ids are unique
		// 3. All prompts must be of a known type
		// 4. Prompt type validation - configuration properties must be valid for the type.
		// 5. Conditions
		// 5a. The first prompt in a contentList cannot have a condition.
		// 5b. Conditions must validate against the condition grammar.
		// 5c. ids in conditions must exist in a prompt previous to the current prompt
		// 5d. The values in conditions must be allowable for the prompt type indicated by the id on the left hand side of the 
		// operation in each condition
		// 
		
		if(args.length < 1) {
			throw new IllegalArgumentException("You must pass a file name as the first argument.");
		}
		
		String fileName = args[0];
		ConfigurationValidator validator = new ConfigurationValidator();
		
		validator.checkSchema(args[0]);
		_logger.info("schema validation successful");
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API
				
		Builder builder = new Builder();
		Document document = builder.build(fileName);
		
//		if(_logger.isDebugEnabled()) {
//			_logger.debug(document.toXML());
//		}
		
		Element root = document.getRootElement();
		
		validator.checkIdUniqueness(root);
		_logger.info("id uniqueness check successful");
		
		validator.checkPromptTypes(root);
		_logger.info("prompt type check successful");
		
		validator.checkPromptTypeProperties(root);
		_logger.info("prompt type values check successful");
				
		validator.checkConditions(root);		
		_logger.info("conditions check successful");
		
		_logger.info("configuration validation successful");
	}
	
	private static Logger _logger = Logger.getLogger(ConfigurationValidator.class);
	private static final String _schemaFile = "spec/configuration.xsd";
	private Map<String, PromptTypeValidator> _promptTypeValidatorMap;
	
	private ConfigurationValidator() {
		_promptTypeValidatorMap = new HashMap<String, PromptTypeValidator>();
	}
	
	/**
	 * Validate an instance document represented by the provided file name against the schema.
	 * 
	 * @throws IOException if the file containing the instance document cannot be found 
	 * @throws IOException if the file containing the instance document cannot be read 
	 * @throws SAXException if schema validation fails  
	 */
	private void checkSchema(String fileName) throws IOException, SAXException {
		StreamSource schemaDocument = new StreamSource(new File(_schemaFile));
		SAXSource instanceDocument = new SAXSource(new InputSource(new FileInputStream(fileName)));
		
		// Originally attempted to use "http://www.w3.org/XML/XMLSchema/v1.1" here, but neither Xerces2 nor the native
		// Java 6 implementation supports it out of the box.  
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);  
		// System.out.println(sf.getClass().getName());
		
		Schema s = sf.newSchema(schemaDocument);
		Validator v = s.newValidator();
		v.validate(instanceDocument);
	}
	
	/**
	 * Checks each id in the instance document for uniqueness.
	 */
	private void checkIdUniqueness(Element root) {
		Nodes idNodes = root.query("//id"); // find all of the id elements in the file
		int size = idNodes.size();
		
		Set<String> stringSet = new HashSet<String>(size);
		
		for(int i = 0; i < size; i++) {
			String value = idNodes.get(i).getValue();
			if(! stringSet.add(value)) { // if add() returns false, it means there is a duplicate in the list
				
				throw new IllegalStateException("Invalid configuration: a duplicate id was found: " + value);
				
			}
		}
	}

	/**
	 * Checks that each condition conforms to our grammar, contains ids that are allowable (conditions can only refer to prompts
	 * or repeatable sets that occur before the current condition and within the current survey), and contains values that are valid
	 * for the prompt type represented by the id.
	 */
	private void checkConditions(Element root) {
		Nodes surveys = root.query("//survey"); // get all surveys
		int numberOfSurveys = surveys.size();
		
		// Now check the conditions within each survey
		for(int x = 0; x < numberOfSurveys; x++) {
			
			Node survey = surveys.get(x);
			Nodes contentList = survey.query("contentList");
			
			int numberOfItemsInContentList = contentList.size();
			
			for(int y = 0; y < numberOfItemsInContentList; y++) {
				// Content lists can contain conditions in prompts, repeatable sets, and prompts in repeatable sets 
				
				Nodes promptsAndRepeatableSets = contentList.get(x).query("prompt | repeatableSet");
				int numberOfOuterElements = promptsAndRepeatableSets.size();
				List<String> idList = new ArrayList<String>();
				
				for(int outerIndex = 0; outerIndex < numberOfOuterElements; outerIndex++) {
					
					Node currentNode = promptsAndRepeatableSets.get(outerIndex);
					String currentId = currentNode.query("id").get(0).getValue(); 
					idList.add(currentId);
					int currentIdIndex = idList.indexOf(currentId);
					
					String currentNodeType = ((Element) currentNode).getLocalName();
					
					if("prompt".equals(currentNodeType)) {
						
						validateCondition(currentNode, outerIndex, currentId, currentIdIndex, idList);
						
					} else { 
						
						 _logger.info("found a repeatableSet");
						 
						 validateCondition(currentNode, outerIndex, currentId, currentIdIndex, idList);
						 
						 // Now check out each prompt in the repeatable set
						 Nodes repeatableSetPromptNodes = currentNode.query("prompt");
						 int numberOfInnerElements = repeatableSetPromptNodes.size();
						 
						 List<String> cumulativeIdList = new ArrayList<String>();
						 cumulativeIdList.addAll(idList);  // copy the id list so it is not changed in the inner loop
						 int cumulativeIndex = outerIndex; // make sure not to increment the outer index
						 						 
						 for(int i = 0; i < numberOfInnerElements; i++, cumulativeIndex++) {
							 
							 Node currentInnerNode = repeatableSetPromptNodes.get(i);
							 String currentInnerId = currentNode.query("id").get(0).getValue();
							 cumulativeIdList.add(currentInnerId);
							 int cumulativeIdIndex = cumulativeIdList.indexOf(currentInnerId);
							 
							 validateCondition(currentInnerNode, cumulativeIdIndex, currentInnerId, cumulativeIdIndex, cumulativeIdList);
						 }
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void validateCondition(Node currentNode, int surveyIndex, String currentId, int currentIdIndex, List<String> idList) {
		Nodes conditionNodes = currentNode.query("condition");
		
		if(conditionNodes.size() > 0) { // conditions are optional
			
			String condition = conditionNodes.get(0).getValue();
			
			if(! "".equals(condition)) { // don't validate an empty node
		
				_logger.info("validating condition for id: " + currentId);
				
				if(0 == surveyIndex) {
					throw new IllegalArgumentException("a condition is not allowed on the first prompt of a " +
						"survey. invalid prompt id: " + currentId);
				}
				
				try {
					// check condition syntax
					Map<String, List<String>> idValuesMap = ConditionValidator.validate(condition);
					
					// check each id to make sure it references a prompt previous to the current prompt
					Set<String> keySet = idValuesMap.keySet();
					Iterator<String> keySetIterator = keySet.iterator();
					
					while(keySetIterator.hasNext()) {
						String key = keySetIterator.next();
						if(idList.indexOf(key) >= currentIdIndex) {
							throw new IllegalStateException("invalid id in condition for prompt id: " + currentId);
						}
						
						// check each value 
						List<String> values = idValuesMap.get(key);
						PromptTypeValidator promptTypeValidator = _promptTypeValidatorMap.get(key);
						
						for(String value : values) {
							promptTypeValidator.validateValue(value);
						}
					}
					
				} catch (ConditionParseException cpe) {
					
					_logger.error("invalid condition at id: " + currentId);
					throw cpe;
				}
			}
		}
	}
	
	
	/**
	 * Checks all prompt types to make sure that they are supported.
	 */
	private void checkPromptTypes(Element root) {
		Nodes promptTypeNodes = root.query("//promptType");
		List<String> stringList = nodesToValueList(promptTypeNodes);
		
		for(String string : stringList) {
			if(! PromptTypeValidatorFactory.isValidPromptType(string)) {
				throw new IllegalStateException("Invalid configuration: an unknown prompt type was found: " + string);
			}
		}
	}
	
	/**
	 * Checks all prompt types to make sure that they have valid configurations.
	 */
	private void checkPromptTypeProperties(Element root) {
		Nodes prompts = root.query("//prompt");
		int size = prompts.size();
		
		for(int i = 0; i < size; i++) {
			// Get the prompt type and then validate the configured properties
			Node promptNode = prompts.get(i);
			String promptId = promptNode.query("id").get(0).getValue();
			_logger.info("validating prompt id: " + promptId);
			String promptType = promptNode.query("promptType").get(0).getValue();
			PromptTypeValidator v = PromptTypeValidatorFactory.getValidator(promptType);
			v.validateAndSetConfiguration(promptNode);
			// add the validator for use in validating condition values
			_promptTypeValidatorMap.put(promptId, v);
		}	
	}
	
	/**
	 * Converts the provided XOM Nodes to a List of values contained in each Node in the Nodes. The XOM API is outdated and 
	 * its Nodes class is not a java.util.List (or some other standard collection).
	 * 
	 * This method can only be used if the Nodes represent Text nodes (instead of say, elements with child elements).
	 */
	private static List<String> nodesToValueList(Nodes nodes) {
		if(null == nodes) {
			return null; // TODO should be an empty immutable list
		}
		
		int size = nodes.size();
		List<String> stringList = new ArrayList<String>();
		
		for(int i = 0; i < size; i++) {
			stringList.add(nodes.get(i).getValue());
		}
		
		return stringList;
	}
}
