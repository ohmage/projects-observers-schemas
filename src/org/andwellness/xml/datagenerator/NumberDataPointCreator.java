package org.andwellness.xml.datagenerator;

import java.util.Map;

import nu.xom.Node;

public class NumberDataPointCreator extends DataPointCreator {
    @Override
    public DataPoint create(Node currentNode) {
        // Grab necessary values from the currentNode
        String displayType = currentNode.query("displayType").get(0).getValue();
        String nodeId = currentNode.query("id").get(0).getValue();
        Map<String, String> nodeProperties;
        int number,min,max;
        
        DataPoint createdDataPoint = new DataPoint();
        
        createdDataPoint.setId(nodeId);
        createdDataPoint.setPromptType(DataPoint.PromptType.number);
        createdDataPoint.setDisplayType(displayType);
        
        // Find the min and max properties
        Node propertyNode = currentNode.query("properties").get(0);
        nodeProperties = extractProperties(propertyNode);
        
        min = Integer.parseInt(nodeProperties.get("min"));
        max = Integer.parseInt(nodeProperties.get("max"));
        
        // Find a random int between max and min
        number = ValueCreator.randomPositiveIntModulus(max - min) + min;
        
        createdDataPoint.setValue(new Integer(number));
        
        if (_logger.isDebugEnabled()) {
            _logger.debug("Creating a number data point with id " + nodeId + " min " + min + " max " + max + " value " + number);
        }
        
        return createdDataPoint;
    }

}
