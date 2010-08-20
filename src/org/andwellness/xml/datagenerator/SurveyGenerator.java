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
                    if (checkCondition(currentNodeCondition)) {
                        DataPoint promptDataPoint = generatePrompt(currentNode, surveyMetadata);
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
                    if (checkCondition(currentNodeCondition)) {
                        List<DataPoint> repeatableSetDataPointList = generateRepeatableSet(currentNode);
                        dataPointList.addAll(repeatableSetDataPointList);
                    }
                }
                
            }
        }
        
        
        return dataPointList;
    }


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
                if (checkCondition(currentInnerCondition)) {
                    // Generate the prompt
                    DataPoint promptDataPoint = generatePrompt(currentInnerNode, repeatableSetMetadata);
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

    private DataPoint generatePrompt(Node currentNode, Map<String, Object> currentMetadata) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean checkCondition(String currentNodeCondition) {
        return true;
    }

    private void insertMetadata(List<DataPoint> dataPointList2,
            Map<String, Object> surveyMetadata2) {
        // TODO Auto-generated method stub
        
    }    
}
