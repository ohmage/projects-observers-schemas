package org.andwellness.xml.datagenerator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        
        
        // Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
        // very simple XPath API
                
        Builder builder = new Builder();
        Document document = builder.build(configFileName);
        Element root = document.getRootElement();
        
        // Create a new generator
        DataGenerator generator = new DataGenerator();
        // Generate the JSON
        JSONArray surveyArray = generator.generateMultipleResponses(root, numberDays, numberSurveysPerDay);
        // Output to a file
        DataGenerator.JSONArrayWriter(outputFileName, surveyArray);
    }
    
    
    // DataGenerator fields go here
    private static Logger _logger = Logger.getLogger(ConfigurationValidator.class);
    
    // DataGenerator methods go here
    private DataGenerator() {};

    /**
     * Utility function to write JSONArrays to file
     *
     * @param fileName Filename to which to write.
     * @param jsonArray The JSONArray to interpret to file.
     * @throws JSONException 
     * @throws IOException 
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
     * Generate one or more surveys starting day and working numberDays backwards.
     * 
     * @param root The XML root that defines the survey data types to generate.
     * @param numberDays The number of days of surveys to generate.
     * @param numberSurveysPerDay THe number of surveys to generate per day.
     * @return A JSONArray containing all the generated surveys.
     */
    public JSONArray generateMultipleResponses(Element root, int numberDays, int numberSurveysPerDay) {
        JSONArray totalResponseArray = new JSONArray();
        
        
        return totalResponseArray;
    }
    
    /**
     * Generate a single survey given the XML configuration and survey creation date.
     * 
     * @param root The XML root that defines the survey data types to generate.
     * @param creationDate The date and time to use to create this survey.
     * @return A JSONArray containing the single created survey.
     */
    public JSONArray generateSingleResponse(Element root, Date creationDate) {
        JSONArray singleResponseArray = new JSONArray();
        
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
        
        return singleResponseArray;
    }
}
