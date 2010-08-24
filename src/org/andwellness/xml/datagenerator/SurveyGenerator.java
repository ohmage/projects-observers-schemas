package org.andwellness.xml.datagenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;

/**
 * SurveyGenerator implements most of the grunt work of translating a single survey described
 * in XML format into a list of data points with randomly generated responses.  The two more
 * complex parts are getting scoping correct with metadata and checking conditions to see
 * if a prompt or repeatableSet should be generated or not.  
 * 
 * For metadata, and datetime in particular, there are three levels of scope.  A data point 
 * should first get its time from its repeatableSet (if it is in a repeatableSet), then from
 * the survey, then finally from the creationTime of the survey.  Since the XML schema dictates
 * that prompt conditions cannot condition on prompts within repeatableSets, we can mimic this scoping
 * fairly easily.  First, generate data points for all the prompts directly under a survey, then go and
 * update all metadata for those prompts, then generate data points for repeatable sets.
 * 
 * @author jhicks
 *
 */
public class SurveyGenerator {
    List<DataPoint> dataPointList = new ArrayList<DataPoint>();  // The DataPoints generated so far
    Map<String, Object> surveyMetadata = new HashMap<String, Object>();  // Holds standard survey metadata
    // General logging
    private static Logger _logger = Logger.getLogger(SurveyGenerator.class);
    
    public SurveyGenerator() {};
    
    /**
     * Generates a list of DataPoints from a survey XOM Node.
     * For each prompt, read in the prompt type and restrictions on the possible data.
     * Generate a random response within those restrictions and add to a Survey.
     * 
     * @param surveyNode An XML node that represents a survey.
     * @param creationTime The survey creation time, used as default datetime metadata.
     * @return A Survey containing randomly generated responses.
     */
    public Survey generateSurvey(Node surveyNode, Date creationTime) {
        // Make sure this is a survey node
        String surveyNodeType = ((Element) surveyNode).getLocalName();
        if (!surveyNodeType.equals("survey")) {
            throw new IllegalArgumentException("The passed Node is not a survey node.");
        }
        
        // Grab the survey id and title for logging
        String surveyId = surveyNode.query("id").get(0).getValue(); 
        String surveyTitle = surveyNode.query("title").get(0).getValue();
        _logger.info("Creating survey id " + surveyId);
        
        // Create the initial Survey
        Survey generatedSurvey = new Survey();
        generatedSurvey.setId(surveyId);
        generatedSurvey.setTitle(surveyTitle);
        generatedSurvey.setCreationTime(creationTime);
        
        // Every survey has a contentList to hold its prompts and repeatableSets
        Nodes contentList = surveyNode.query("contentList");
        int numberOfItemsInContentList = contentList.size();
        
        for (int x = 0; x < numberOfItemsInContentList; x++) {
            // To get metadata scoping correct, first grab all the prompts, generate their responses, add
            // in the metadata, then grab the repeatableSets and do it again
            Nodes promptsOrRepeatableSets = contentList.get(x).query("prompt | repeatableSet");
            int numberOfPromptsOrRepeatableSets = promptsOrRepeatableSets.size();
            
            // loop over each prompt
            for (int promptIndex = 0; promptIndex < numberOfPromptsOrRepeatableSets; promptIndex++) {
                
                Node currentNode = promptsOrRepeatableSets.get(promptIndex);
                String currentNodeId = currentNode.query("id").get(0).getValue(); 
                String currentNodeType = ((Element) currentNode).getLocalName();
                String currentNodeCondition = currentNode.query("condition").get(0).getValue();
                
                if("prompt".equals(currentNodeType)) {
                    _logger.info("found a prompt with id " + currentNodeId);
                    
                    // Check the condition to see if this prompt should generate a data point
                    if (checkCondition(currentNodeCondition, dataPointList)) {
                        DataPoint dataPoint = generatePrompt(currentNode);
                        generatedSurvey.addResponse(dataPoint);
                    }              
                }
                if ("repeatableSet".equals(currentNodeType)) {
                    _logger.info("found a repeatableSet with id " + currentNodeId);
                    
                    // Check the condition to see if this repeatableSet should be run
                    if (checkCondition(currentNodeCondition, generatedSurvey.getCurrentDataPoints())) {
                        RepeatableSet generatedRepeatableSet = new RepeatableSet();
                        generatedRepeatableSet.setId(currentNodeId);
                        
                        // Each repeatableSet ends with a true/false continue question.  Continue to
                        // generate sets until this is false.
                        do {
                            List<DataPoint> repeatableSetDataPointList = generateRepeatableSet(currentNode);
                            generatedRepeatableSet.addSet(repeatableSetDataPointList);
                        } 
                        while(ValueCreator.randomBoolean());
                        
                        generatedSurvey.addResponse(generatedRepeatableSet);
                    }
                }
                else {
                    _logger.error("Found a bad XML node in the contentList with id " + currentNodeId + " and type " + currentNodeType);
                }
            }
        }
        
        
        return generatedSurvey;
    }


    /**
     * Pass in a Node of type repeatableSet.  The function will create a List of DataPoints with
     * randomly generated responses.
     * 
     * @param currentNode The repeatableSet to generate.
     * @param creationTime A Date to represent the parent survey's creationTime
     * @return A List of DataPoints generated.
     */
    private List<DataPoint> generateRepeatableSet(Node currentNode) {
        String currentNodeId = currentNode.query("id").get(0).getValue();
        String currentNodeType = ((Element) currentNode).getLocalName();
        List<DataPoint> promptDataPointList = new ArrayList<DataPoint>();
        
        
        // Make sure this is a repeatable set
        if (!"repeatableSet".equals(currentNodeType)) {
            throw new IllegalArgumentException("Need a repeatableSet, instead found a node of type " + currentNodeType + " with id " + currentNodeId);
        }
        
        // Check out each prompt in the repeatable set
        Nodes repeatableSetPromptNodes = currentNode.query("prompt");
        int numberOfInnerElements = repeatableSetPromptNodes.size();
                                
        for(int i = 0; i < numberOfInnerElements; i++) {
            
            Node currentInnerNode = repeatableSetPromptNodes.get(i);
            String currentInnerId = currentNode.query("id").get(0).getValue();
            String currentInnerType = ((Element) currentNode).getLocalName();
            String currentInnerCondition = currentNode.query("condition").get(0).getValue();
            
            // Make sure this is a prompt
            if ("prompt".equals(currentInnerType)) {
                _logger.info("found a prompt with id " + currentInnerId);
                
                // Make sure the condition is valid
                if (checkCondition(currentInnerCondition, promptDataPointList)) {
                    // Generate the prompt
                    DataPoint promptDataPoint = generatePrompt(currentInnerNode);
                    promptDataPointList.add(promptDataPoint);
                }
            }
            else {
                _logger.error("Found a bad XML node in the repeatableSet with id " + currentInnerId + " and type " + currentInnerType);
            }
        }
        
        return promptDataPointList;
    }

    /**
     * Generate a single DataPoint from a single prompt.  The DataPoint will only have an id and a value, no
     * metadata will be added
     * 
     * @param currentNode The prompt node to generate.
     * @param creationTime The time the prompt was created.
     * @return A DataPoint with the prompt response randomly generated based on the prompt properties and prompt type.
     */
    private DataPoint generatePrompt(Node currentNode) {
        String promptType = currentNode.query("promptType").get(0).getValue();
        DataPointCreator dataPointCreator = DataPointCreatorFactory.getDataPointCreator(promptType);
        
        DataPoint createdDataPoint = dataPointCreator.create(currentNode);
        
        if (_logger.isDebugEnabled()) {
            _logger.debug("Created a datapoint: " + createdDataPoint.toString());
        }
        
        return createdDataPoint;
    }

    /**
     * If the dataPoint is of displayType metadata, update the metadata with its value.
     * 
     * @param dataPoint The datapoint to check.
     * @param metadata Update the metadata if the data point is labeled as metadata.
     * @param creationTime Needed to translate hours_before_now data points into timestamps (creationTime is the "now")
     */
    /*
    private void updateMetadata(DataPoint dataPoint, Map<String, Object> metadata) {
        String timestamp;
        
        // First check if this is of displayType metadata
        if (!dataPoint.isMetadata()) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("DataPoint " + dataPoint.getId() + " is not metadata.");
                return;
            }
        }
        
        _logger.debug("DataPoint" + dataPoint.getId() + "is metadata.");
        
        // Only handle timestamps or hours_before_now for now, should not really be
        // directly accessing prompt types here in case they change later
        if (DataPoint.PromptType.timestamp.equals(dataPoint.getPromptType())) {
            timestamp = (String) dataPoint.getValue();
            metadata.put("datetime", timestamp);
        }
        
        // Very hacky, need to translate hours_before_now into a time stamp, not sure where this
        // should actually be done
        if (DataPoint.PromptType.hours_before_now.equals(dataPoint.getPromptType())) {
            String hoursBeforeNow = (String) dataPoint.getValue();
            timestamp = ValueCreator.hours_before_date(creationTime, Integer.parseInt(hoursBeforeNow));
            metadata.put("datetime", timestamp);
        }
        
    }
    */
    
    /**
     * Check if the condition is true based on previous node responses.  If the id does
     * not exist in the previous responses, assume the response is NULL.
     * 
     * @param currentNodeCondition The condition to check, needs to be parsed.
     * @param previousResponses The List of previous responses.
     * @return Whether or not the condition is true.
     */
    private boolean checkCondition(String currentNodeCondition, List<DataPoint> previousResponses) {
        return true;
    }

    /**
     * Go through every data point in the list and update its metadata with the passed metadata.
     * 
     * @param dataPoints The data points to update.
     * @param metadata The metadata to use to update the datapoint. 
     */
    /*
    private void insertMetadata(List<DataPoint> dataPoints,
            Map<String, Object> metadata) {
        
        Iterator<DataPoint> dataPointsIterator = dataPoints.iterator();
        while (dataPointsIterator.hasNext()) {
            DataPoint currentDataPoint = dataPointsIterator.next();
            
            if (metadata.containsKey("datetime")) {
                currentDataPoint.setDatetime((String) metadata.get("datetime"));
            }
            if (metadata.containsKey("tz")) {
                currentDataPoint.setTz((String) metadata.get("tz"));          
            }
            if (metadata.containsKey("lat")) {
                currentDataPoint.setLat((String) metadata.get("lat"));
            }
            if (metadata.containsKey("lon")) {
                currentDataPoint.setLon((String) metadata.get("lon"));
            }            
        }
        
    }  
    */  
}
