package org.andwellness.xml.datagenerator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.xml.ConfigurationValidator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Procedural class to generate JSON from a validated XML survey configuration file.
 * 
 * @author jhicks
 *
 */
public class DataGenerator {
    /**
     * args[0]: the file name of the file from which to generate data
     * args[1]: The number of days of data to generate (starting today and going backwards)
     * args[2]: The number of surveys per day to generate, evenly spaced throughout the day
     * args[3]: The file name of the file to which to output the JSON
     * args[4]: Indicate whether to output JSON that mimics in to server or out of server communication, possible values are <in> or <out>
     * @throws IOException 
     * @throws ParsingException 
     * @throws ValidityException 
     * @throws JSONException 
     */
    public static void main(String[] args) throws ValidityException, ParsingException, IOException, JSONException {
        // Lcoally store the arguments
        String configFileName, outputFileName, jsonType;
        int numberDays, numberSurveysPerDay;
        
        // Configure log4j. (pointing to System.out)
        BasicConfigurator.configure();
        
        if(args.length < 5) {
            throw new IllegalArgumentException("Incorrect number of arguments.");
        }
        
        // Grab the fileNames, no fancy checking of formatting
        configFileName = args[0];
        outputFileName = args[3];
        jsonType = args[4];
        
        try {
            numberDays = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("The second argument must be an integer.");
        }
        
        try {
            numberSurveysPerDay = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("The third argument must be an integer.");
        }
        
        // Init the json generator based on
        
        // Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
        // very simple XPath API
                
        Builder builder = new Builder();
        Document document = builder.build(configFileName);
        Element root = document.getRootElement();
        
        // Create a new generator and generate a list of DataPoints
        DataGenerator generator = new DataGenerator();
        List<DataPoint> surveyArray = generator.generateMultipleResponses(root, numberDays, numberSurveysPerDay);
        
        // Translate the list of data points to JSON based on the passed in argument jsonType
        JSONGeneratorType jsonGenerator = JSONGeneratorTypeFactory.getGenerator(jsonType);
        JSONArray finalJSONArray = jsonGenerator.translateDataPointsToJsonArray(surveyArray);
        
        // Output to a file
        DataGenerator.JSONArrayWriter(outputFileName, finalJSONArray);
    }
    
    
    // DataGenerator fields go here
    private static Logger _logger = Logger.getLogger(DataGenerator.class);
    
    
    // DataGenerator methods go here
    private DataGenerator() {};

    /**
     * Utility function to write JSONArrays to file
     *
     * @param fileName Filename to which to write.
     * @param jsonArray The JSONArray to interpret to file.
     * @throws JSONException If there is a problem in the formatting of the JSONArray
     * @throws IOException If the output file cannot be accessed (permissions, disk full)
     */
    public static void JSONArrayWriter(String fileName, JSONArray jsonArray) throws JSONException, IOException {
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName)));
        
        System.out.println(jsonArray.toString(4));
        
        String jsonString = jsonArray.toString();
        
        int strLen = jsonString.length();
        
        int start = 0;
        int chunkSize = 1024;
        
        while(start < strLen) {
            
            int amountLeft = strLen - start;
            int amountToWrite = chunkSize > amountLeft ? amountLeft : chunkSize; 
            
            out.write(jsonString, start, amountToWrite);
            start += chunkSize;
        }
        
        out.write("\n");
        out.flush();
        out.close();
    }
    
    
    /**
     * Generate random responses using multiple passes over the XML file.
     * Creates numberDays x numberSurveysPerDay responses, equally spaced over each day.
     * 
     * @param root The XML root that defines the survey data types to generate.
     * @param numberDays The number of days of surveys to generate.
     * @param numberSurveysPerDay THe number of surveys to generate per day.
     * @return A list of DataPoints containing the values and metadata.
     */
    public List<DataPoint> generateMultipleResponses(Element root, int numberDays, int numberSurveysPerDay) {
        List<DataPoint> responseList = new ArrayList<DataPoint>();
        
        // Hack this in here to get one response working for now
        responseList.addAll(generateSingleResponse(root, new Date()));
        
        return responseList;
    }
    
    /**
     * Generate a random responses as a single pass over the XML file.
     * 
     * @param root The XML root that defines the survey data types to generate.
     * @param creationDate The date and time to use to create this survey.
     * @return A JSONArray containing the single created survey.
     */
    public List<DataPoint> generateSingleResponse(Element root, Date creationDate) {
        List<DataPoint> responseList = new ArrayList<DataPoint>();
        
        Nodes surveys = root.query("//survey"); // get all surveys
        int numberOfSurveys = surveys.size();
        
        // Loop over each prompt within the survey
        for (int x = 0; x < numberOfSurveys; x++) {            
            Node survey = surveys.get(x);
            
            
            
            
            
            Nodes contentList = survey.query("contentList");
            
            int numberOfItemsInContentList = contentList.size();
            
            // Store a list of all prompt IDs in the survey that have been generated
            Map<String, String> currentPromptResponses = new HashMap<String, String>();
            
            for(int y = 0; y < numberOfItemsInContentList; y++) {
                // Content lists can contain conditions in prompts, repeatable sets, and prompts in repeatable sets 
                
                Nodes promptsAndRepeatableSets = contentList.get(y).query("prompt | repeatableSet");
                int numberOfOuterElements = promptsAndRepeatableSets.size();
                List<String> idList = new ArrayList<String>();
                
                for(int outerIndex = 0; outerIndex < numberOfOuterElements; outerIndex++) {
                    
                    Node currentNode = promptsAndRepeatableSets.get(outerIndex);
                    String currentNodeId = currentNode.query("id").get(0).getValue(); 
                    String currentNodeType = ((Element) currentNode).getLocalName();
                    String currentNodeCondition = currentNode.query("condition").get(0).getValue();
                    
                    
                    if("prompt".equals(currentNodeType)) {
                        _logger.info("found a prompt with id " + currentNodeId);
                        // Turn the prompt into JSON and append onto our array
                        if (checkCondition(currentNodeCondition, currentPromptResponses)) {
                            JSONArray singlePromptArray = generatePrompt(currentNode, creationDate);
                            // Add to the current list of prompts generated
                            //String responseValue = singlePromptArray.
                            //currentPromptResponses.put(currentNodeId, responseValue);
                            // Append the generated data on to the JSONArray
                            //DataGenerator.appendJSONArray(singleResponseArray, singlePromptDataPoint.)
                        }
                        
                    } else { 
                        
                         _logger.info("found a repeatableSet");
                         
                         //validateCondition(currentNode, outerIndex, currentId, currentIdIndex, idList);
                         
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
                             
                             //validateCondition(currentInnerNode, cumulativeIdIndex, currentInnerId, cumulativeIdIndex, cumulativeIdList);
                         }
                    }
                }
            }
        }
        
        return responseList;
    }

    private boolean checkCondition(String currentNodeCondition,
            Map<String, String> currentPromptResponses) {
        // TODO Implement condition checking, for now assume always true
        return true;
    }

    private JSONArray generatePrompt(Node currentNode, Date creationDate) {
        // TODO Auto-generated method stub
        return null;
    }
}
