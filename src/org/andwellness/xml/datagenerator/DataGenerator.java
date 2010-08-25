package org.andwellness.xml.datagenerator;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.BasicConfigurator;
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
        List<Survey> surveyList = generator.generateMultipleResponses(root, numberDays, numberSurveysPerDay);
        
        // Translate the list of Surveys to JSON based on the passed in argument jsonType
        JSONGeneratorType jsonGenerator = JSONGeneratorTypeFactory.getGenerator(jsonType);
        JSONArray finalJSONArray = new JSONArray();
        
        Iterator<Survey> surveyListIterator = surveyList.iterator();
        while (surveyListIterator.hasNext()) {
            Survey survey = surveyListIterator.next();
            
            JSONArray surveyJSONArray = jsonGenerator.translateSurveyToJsonArray(survey);
            appendJSONArray(finalJSONArray, surveyJSONArray);
        }
        
        
        // Output to a file
        DataGenerator.JSONArrayWriter(outputFileName, finalJSONArray);
    }
    
    
    // DataGenerator fields go here
    //private static Logger _logger = Logger.getLogger(DataGenerator.class);
    
    
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
     * @return A list of Surveys.
     */
    public List<Survey> generateMultipleResponses(Element root, int numberDays, int numberSurveysPerDay) {
        List<Survey> responseList = new ArrayList<Survey>();
        
        // Find the number of seconds between surveys to get the current number of
        // surveys per day
        int numberSecondsBetweenSurveys = (60 * 60 * 24) / numberSurveysPerDay;
        Calendar calendar = new GregorianCalendar();  // Use for Date calculations
        
        // Now create numberDays x numberSurveysPeyDay, starting now and going back by the calculated seconds
        for (int i = 0; i < numberDays * numberSurveysPerDay; ++i) {
            // Generate a single response at this time
            responseList.addAll(generateSingleResponse(root, calendar.getTime()));
            
            // Subtract the seconds
            calendar.add(Calendar.SECOND, -1 * numberSecondsBetweenSurveys);
        }
        
        return responseList;
    }
    
    /**
     * Generate a random responses as a single pass over the XML file.
     * 
     * @param root The XML root that defines the survey data types to generate.
     * @param creationDate The date and time to use to create this survey.
     * @return A Survey with the created data points.
     */
    public List<Survey> generateSingleResponse(Element root, Date creationDate) {
        List<Survey> generatedSurveys = new ArrayList<Survey>();
        
        Nodes surveys = root.query("//survey"); // get all surveys
        int numberOfSurveys = surveys.size();
        
        // Loop over each prompt within the survey
        for (int x = 0; x < numberOfSurveys; x++) {            
            Node survey = surveys.get(x);
            
            // Create a new SurveyGenerator and create the survey
            SurveyGenerator surveyGenerator = new SurveyGenerator();
            generatedSurveys.add(surveyGenerator.generateSurvey(survey, creationDate));
        }
        
        return generatedSurveys;
    }
    
    /**
     * Utility function to append on JSONArray on to another.  Not at all efficient, does a one by one
     * copy from one array to the other.
     * 
     * @param appendee Append onto this array.
     * @param appender Append from this array.
     */
    private static void appendJSONArray(JSONArray appendee, JSONArray appender) {
        int appenderSize = appender.length();
        for (int i = 0; i < appenderSize; ++i) {
            try {
                appendee.put(appender.get(i));
            // This should never break, but if it does just continue on
            } catch (JSONException e) {
                continue;
            }
        }
    }
}
