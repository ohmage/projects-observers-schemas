package org.andwellness.xml.datagenerator;

import java.util.Map;

import org.apache.log4j.Logger;

import nu.xom.Node;

public class SingleChoiceDataPointCreator extends DataPointCreator {
    private Logger _logger = Logger.getLogger(SingleChoiceDataPointCreator.class);
    
    @Override
    public DataPoint create(Node currentNode) {
        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        Map<String, String> nodeProperties;
        int numberKeys, chosenKeyNumber, chosenValue;
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.single_choice);
        createdDataPoint.setDisplayType(displayType);

        // Find all node properties, randomly choose one key as the node value
        Node propertyNode = currentNode.query("properties").get(0);
        nodeProperties = extractProperties(propertyNode);        
        numberKeys = nodeProperties.size();
        
        // Select a random number between 0 and the size of the keySet - 1, and use that to
        // select the key
        chosenKeyNumber = ValueCreator.randomPositiveIntModulus(numberKeys);
        chosenValue = Integer.parseInt((String) nodeProperties.keySet().toArray()[chosenKeyNumber]);
        
        createdDataPoint.setValue(new Integer(chosenValue));
        
        if (_logger.isDebugEnabled()) {
            _logger.debug("Creating a single choice data point with id " + nodeId + " value " + chosenValue);
        }
        
        return createdDataPoint;
    }

}
