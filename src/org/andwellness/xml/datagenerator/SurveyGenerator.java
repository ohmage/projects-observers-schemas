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
     * Generates a list of DataPoints from a survey Node.
     * The SurveyGenerator will automatically create and keep track of necessary metadata including
     * datetime, lat/lon, and timezone.  
     * 
     * @param surveyNode An XML node that represents a survey.
     * @param metadataTime The time we should use for now.
     * @return A List of DataPoints representing randomly generated responses to the survey.
     */
    public List<DataPoint> generateSurvey(Node surveyNode, Date metadataTime) {
        // Make sure this is a survey node
        String surveyNodeType = ((Element) surveyNode).getLocalName();
        if (!surveyNodeType.equals("survey")) {
            throw new IllegalArgumentException("The passed Node is not a survey node.");
        }
        
        // Grab the survey id for logging
        String surveyId = surveyNode.query("id").get(0).getValue(); 
        _logger.info("Creating survey id " + surveyId);
        
        // Create the initial metadata
        surveyMetadata.put("datetime", ValueCreator.date(metadataTime));
        surveyMetadata.put("tz", ValueCreator.tz());
        surveyMetadata.put("lat", new Double(ValueCreator.latitude()));
        surveyMetadata.put("lon", new Double(ValueCreator.longitude()));
        
        // Every survey has a contentList to hold its prompts and repeatableSets
        Nodes contentList = surveyNode.query("contentList");
        int numberOfItemsInContentList = contentList.size();
        
        for (int x = 0; x < numberOfItemsInContentList; x++) {
            // To get metadata scoping correct, first grab all the prompts, generate their responses, add
            // in the metadata, then grab the repeatableSets and do it again
            Nodes prompts = contentList.get(x).query("prompt");
            int numberOfPrompts = prompts.size();
            
            // loop over each prompt
            for (int promptIndex = 0; promptIndex < numberOfPrompts; promptIndex++) {
                
                Node currentNode = prompts.get(promptIndex);
                String currentNodeId = currentNode.query("id").get(0).getValue(); 
                String currentNodeType = ((Element) currentNode).getLocalName();
                String currentNodeCondition = currentNode.query("condition").get(0).getValue();
                
                if("prompt".equals(currentNodeType)) {
                    _logger.info("found a prompt with id " + currentNodeId);
                    
                    // Check the condition to see if this prompt should generate a data point
                    if (checkCondition(currentNodeCondition, dataPointList)) {
                        DataPoint promptDataPoint = generatePrompt(currentNode);
                        updateMetadata(promptDataPoint, surveyMetadata);
                        dataPointList.add(promptDataPoint);
                    }              
                }
                else {
                    _logger.error("Found a bad XML node in the contentList with id " + currentNodeId + " and type " + currentNodeType);
                }
            }
            
            // Go and add all metadata to the generated data points
            insertMetadata(dataPointList, surveyMetadata);
            
            // repeatableSets have their own scope for metadata, do them after we insert the metadata
            Nodes repeatableSets = surveyNode.query("repeatableSet");
            int numberOfRepeatableSets = repeatableSets.size();
            
            // loop over each repeatableSet
            for (int repeatableSetIndex = 0; repeatableSetIndex < numberOfRepeatableSets; repeatableSetIndex++) {
                
                Node currentNode = prompts.get(repeatableSetIndex);
                String currentNodeId = currentNode.query("id").get(0).getValue(); 
                String currentNodeType = ((Element) currentNode).getLocalName();
                String currentNodeCondition = currentNode.query("condition").get(0).getValue();
                
                if ("repeatableSet".equals(currentNodeType)) { 
                    
                    _logger.info("found a repeatableSet with id " + currentNodeId);
                    
                    // Check the condition to see if this repeatableSet should be run
                    if (checkCondition(currentNodeCondition, dataPointList)) {
                        List<DataPoint> repeatableSetDataPointList = generateRepeatableSet(currentNode);
                        dataPointList.addAll(repeatableSetDataPointList);
                    }
                }
                
            }
        }
        
        
        return dataPointList;
    }


    /**
     * Pass in a Node of type repeatableSet.  The function will create a List of DataPoints with
     * randomly generated responses.  RepatableSets use their own metadata scope.
     * 
     * @param currentNode The repeatableSet to generate.
     * @return A List of DataPoints generated.
     */
    private List<DataPoint> generateRepeatableSet(Node currentNode) {
        String currentNodeId = currentNode.query("id").get(0).getValue();
        String currentNodeType = ((Element) currentNode).getLocalName();
        @SuppressWarnings("unchecked")
        Map<String, Object> repeatableSetMetadata = (Map<String, Object>)((HashMap<String, Object>)surveyMetadata).clone();
        List<DataPoint> promptDataPointList = new ArrayList<DataPoint>();
        
        
        // Make sure this is a repeatable set
        if (!"repeatableSet".equals(currentNodeType)) {
            throw new IllegalArgumentException("Need a repeatableSet, found instead a " + currentNodeType + " with id " + currentNodeId);
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
                    updateMetadata(promptDataPoint, repeatableSetMetadata);
                    promptDataPointList.add(promptDataPoint);
                }
            }
            else {
                _logger.error("Found a bad XML node in the repeatableSet with id " + currentInnerId + " and type " + currentInnerType);
            }
        }
        
        // Now go back and update all the data points with the metadata
        insertMetadata(promptDataPointList, repeatableSetMetadata);
        
        return promptDataPointList;
        
    }

    /**
     * Generate a single DataPoint from a single prompt.
     * 
     * @param currentNode The prompt node to generate.
     * @return A DataPoint with the prompt response randomly generated based on the prompt properties and prompt type.
     */
    private DataPoint generatePrompt(Node currentNode) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * If the dataPoint is of displayType metadata, update the metadata with its value.
     * 
     * @param dataPoint The datapoint to check.
     * @param metadata Update the metadata if the data point is labeled as metadata.
     */
    private void updateMetadata(DataPoint dataPoint, Map<String, Object> metadata) {
        
    }
    
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
     * @param surveyMetadata2 
     */
    private void insertMetadata(List<DataPoint> dataPoints,
            Map<String, Object> metadata) {
        // TODO Auto-generated method stub
        
    }    
}
