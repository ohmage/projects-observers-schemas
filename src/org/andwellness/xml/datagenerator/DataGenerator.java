package org.andwellness.xml.datagenerator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
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
     * @throws IOException 
     * @throws ParsingException 
     * @throws ValidityException 
     * @throws JSONException 
     */
    public static void main(String[] args) throws ValidityException, ParsingException, IOException, JSONException {
        String fileName;
        int numberDays;
        int numberSurveysPerDay;
        
        // Configure log4j. (pointing to System.out)
        BasicConfigurator.configure();
        
        if(args.length < 3) {
            throw new IllegalArgumentException("Incorrect number of arguments.");
        }
        
        // Grab the arguments, no fancy checking of formatting
        fileName = args[0];
        
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
        Document document = builder.build(fileName);
        Element root = document.getRootElement();
        
        // Create a new generator
        DataGenerator generator = new DataGenerator();
        // Generate the JSON
        JSONArray surveyArray = generator.generateMultipleSurveys(root, numberDays, numberSurveysPerDay);
        // Output to a file
        DataGenerator.JSONArrayWriter(fileName, surveyArray);
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
    public JSONArray generateMultipleSurveys(Element root, int numberDays, int numberSurveysPerDay) {
        
        return null;
    }
    
    /**
     * Generate a single survey given the XML configuration and survey creation date.
     * 
     * @param root The XML root that defines the survey data types to generate.
     * @param creationDate The date and time to use to create this survey.
     * @return A JSONArray containing the single created survey.
     */
    public JSONArray generateSingleSurvey(Element root, Date creationDate) {
        // TODO Implement this function
        return null;
    }
}
