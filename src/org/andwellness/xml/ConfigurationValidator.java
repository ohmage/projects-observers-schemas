package org.andwellness.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
		
		validator.checkPromptTypeValues(root);
		_logger.info("prompt type values check successful");
				
		validator.checkConditions(root);		
		_logger.info("conditions check successful");
		
		_logger.info("configuration validation successful");
	}
	
	private static Logger _logger = Logger.getLogger(ConfigurationValidator.class);
	private static final String _schemaFile = "spec/configuration.xsd";
	private List<String> _promptTypes;
	
	private ConfigurationValidator() {
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
				int numberOfOuterConditionedElements = promptsAndRepeatableSets.size();
				List<String> idList = new ArrayList<String>();
				
				for(int z = 0; z < numberOfOuterConditionedElements; z++) {
					Node currentNode = promptsAndRepeatableSets.get(z);
					String currentId = currentNode.query("id").get(0).getValue(); 
					idList.add(currentId);
					int currentIdIndex = idList.indexOf(currentId);
					
					String currentNodeType = ((Element) currentNode).getLocalName();
					
					if("prompt".equals(currentNodeType)) {
						
						// _logger.info("found a prompt");
						
						Nodes conditionList = currentNode.query("condition"); // query() always returns a list
						String promptType = currentNode.query("promptType").get(0).getValue();
						
						if(conditionList.size() > 0) { // conditions are optional
							String condition = conditionList.get(0).getValue();
							
							if(! "".equals(condition)) { // make sure there is not an empty node
								
								// make sure there isn't a condition in the first prompt
								
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
										for(String value : values) {
											// if the prompt type is text, timestamp, or photo the only value allowed is SKIPPED
											// if the prompt type is any other type, a number (dependent on the type's restrictions)
											// and SKIPPED are allowed.
											
											if("SKIPPED".equals(value)) {
												
												continue;
												
											} else {
												
												String conditionIdPromptType = getPromptTypeForId(root, key); 
												// _logger.info("promptType=" + conditionIdPromptType);
												
												if("timestamp".equals(conditionIdPromptType) 
												   || "text".equals(conditionIdPromptType)   
												   || "photo".equals(conditionIdPromptType)) {
													
													throw new IllegalStateException("invalid value" + value + "for prompt type in" +
													    " condition statement: " + condition);
												}
												
												int intValue = 0;
												
												try {
												
													intValue = Integer.parseInt(value);
													
												} catch(NumberFormatException nfe) {
													
													throw new IllegalStateException("condition value" + value + " must be a number for prompt id: " + currentId);
													
												}
												
												isValidValueForPrompt(root, key, intValue);
												
											}
										}
									}
									
								} catch (ConditionParseException cpe) {
									
									_logger.error("invalid condition at prompt id: " + currentId);
									throw cpe;
								}
							}
						}
						
					} else { // it's a repeatable set
						
						
					}
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
			
			if(! _promptTypes.contains(string)) {
			
				throw new IllegalStateException("Invalid configuration: an unknown prompt type was found: " + string);
			}
		}
	}
	
	/**
	 * Checks all prompt types to make sure that they have valid configurations.
	 */
	private void checkPromptTypeValues(Element root) {
		Nodes prompts = root.query("//prompt");
		int size = prompts.size();
		
		for(int i = 0; i < size; i++) {
			
			// Get the prompt type and then validate the configured properties
			
			Node promptNode = prompts.get(i);
			String promptId = promptNode.query("id").get(0).getValue();
			String promptType = promptNode.query("promptType").get(0).getValue();
			
			if("number".equals(promptType) || "hours_before_now".equals(promptType)) {
				
				// A min and max are required and they must be numbers
				
				isValidNegOrPosInteger(promptNode.query("properties/p/k[text()='min']").get(0).getParent().query("v"), "min", promptId);
				isValidNegOrPosInteger(promptNode.query("properties/p/k[text()='max']").get(0).getParent().query("v"), "max", promptId);
				
			} else if(("single_choice").equals(promptType) 
					 || ("multi_choice").equals(promptType)
					 || ("single_choice_custom").equals(promptType) 
					 || ("multi_choice_custom").equals(promptType)) {
				
				// At least two k-v pairs must be present
				// All k must be number
				// 'v' nodes exist at this point because of schema validation
				
				Nodes kNodes = promptNode.query("properties/p/k");
				if(kNodes.size() < 2) {
					throw new IllegalStateException("At least 2 'p' nodes are required, prompt id: " + promptId);
				}
				int kSize = kNodes.size();
				for(int j = 0; j < kSize; j++) {
					String kString = kNodes.get(j).getValue();
					if(! isValidPosInteger(kString)) {
						throw new IllegalStateException("Invalid 'k' node for prompt id: " + promptId);
					}
				}
				
			} else if(("photo").equals(promptType)) {
				
				// At least one k-v pair must be present
				// k must equal "res"
				// v must be a positive integer
				Nodes pNodes = promptNode.query("properties/p");
				if(pNodes.size() != 1) {
					throw new IllegalArgumentException("Missing or extra photo property (one 'res' is required) for prompt id: " + promptId);
				}
				
				Nodes vNodes = promptNode.query("properties/p/v");
				String vString = vNodes.get(0).getValue();
				if(! isValidPosInteger(vString)) {
					throw new IllegalStateException("Missing 'v' for 'res' property for prompt id: " + promptId);
				}
			}
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
			return null; // should be an empty immutable list
		}
		
		int size = nodes.size();
		List<String> stringList = new ArrayList<String>();
		
		for(int i = 0; i < size; i++) {
			stringList.add(nodes.get(i).getValue());
		}
		
		return stringList;
	}
	
	/**
	 * Returns the prompt type for the prompt identified by the provided id. 
	 */
	private String getPromptTypeForId(Element root, String id) {
		Node promptNode = root.query("//id[text()='" + id + "']").get(0);
		return promptNode.getParent().query("promptType").get(0).getValue();
	}

	private void isValidNegOrPosInteger(Nodes nodes, String text, String promptId) {
		if(1 != nodes.size()) {
			throw new IllegalStateException("missing or extra " + text  + " property for prompt: " + promptId);
		} else {
			String v = nodes.get(0).getValue();
			try {
				Integer.parseInt(v);
			} catch (NumberFormatException nfe) {
				throw new IllegalStateException("invalid " + text + " property [value=" + v + "] for prompt: " + promptId);
			}
		}
	}
	
	private boolean isValidPosInteger(String v) {
		int i = 0;
		try {
			i = Integer.parseInt(v);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return i < 0 ? false : true; 
	}
	
	/**
	 * Returns the prompt type for the prompt identified by the provided id. 
	 */
	private boolean isValidValueForPrompt(Element root, String id, int value) {
		String promptType = getPromptTypeForId(root, id);
		
		
		
		
		
		return true;
		
	}
}
